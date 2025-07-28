package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;


public class SimpleServer extends AbstractServer {


	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private static ArrayList<ConnectedUser> ConnectedList = new ArrayList<>();
	DatabaseManager databaseManager = new DatabaseManager();

	public SimpleServer(int port) {
		super(port);
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.out.println("❌ SQLite JDBC driver not found.");
			e.printStackTrace();
		}
		DatabaseManager.connect(); // Connect using DatabaseManager
		DatabaseInitializer.initializeDatabase();
		notifyExpiringSubscriptions();

	}


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (msg instanceof String) {

			String text = (String) msg;
			System.out.println(text);

			System.out.println("Database users: ");
			DatabaseManager.printAllUsers();
			System.out.println("Connected users: ");

			for (ConnectedUser cU : ConnectedList) {
				System.out.println("🟢 Connected: " +
						"Username = " + cU.getUsername());
			}

			if (text.startsWith("GET_CATALOG")) {
				// Client requested the full catalog
				SubscribedClient exists = findClient(client);
				if (exists == null) {
					SubscribedClient connection = new SubscribedClient(client);
					SubscribersList.add(connection);
				}
				databaseManager.sendCatalog(client);

			} else if (text.startsWith("GET_ITEM")) {
				// Format: GET_ITEM:<id>
				String[] parts = text.split(":");
				if (parts.length == 2) {
					int id = Integer.parseInt(parts[1]);
					sendItem(client, id);
				}

			} else if (text.startsWith("UPDATE_PRICE")) {
				// Format: UPDATE_PRICE:<id>:<newPrice>
				String[] parts = text.split(":");
				if (parts.length == 3) {
					int id = Integer.parseInt(parts[1]);
					double newPrice = Double.parseDouble(parts[2]);
					ChangePriceEvent event = DatabaseManager.updatePrice(client, id, newPrice);
					sendToAllClients(event);
				}

			} else if (text.startsWith("remove client")) {
				SubscribedClient toRemove = findClient(client);
				if (toRemove != null) {
					SubscribersList.remove(toRemove);
					if (!toRemove.getUsername().equals("~"))
						for (ConnectedUser user : ConnectedList) {
							if (user.getUsername().equals(toRemove.getUsername())) {
								ConnectedUser userToRemove = user;
								ConnectedList.remove(userToRemove);

								break;
							}
						}
				}

			}
			else if (text.startsWith("SUBSCRIBE:")) {
				// פורמט: SUBSCRIBE:<id>
				String[] parts = text.split(":");
				if (parts.length == 2) {
					try {
						int userId = Integer.parseInt(parts[1]);
						java.time.LocalDate start = java.time.LocalDate.now();
						java.time.LocalDate end   = start.plusYears(1);

						// יצירה/עדכון מנוי לשנה
						DatabaseManager.upsertSubscription(userId, start, end);
						// רישום המכירה ליום הנוכחי (100₪ לפי הדרישה – החישוב הכספי בדו״ח)
						DatabaseManager.addSubscriptionSale(start);

						client.sendToClient("SUBSCRIBE_OK");

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			else if (text.startsWith("LOGOUT:")) {
				String uname = text.substring("LOGOUT:".length());

				// הסרת המשתמש המחובר
				ConnectedList.removeIf(u -> u.getUsername().equals(uname));

				// נתק את השם מה-SubscribedClient הנוכחי (החיבור נשאר "אנונימי")
				SubscribedClient sc = findClient(client);
				if (sc != null) sc.setUsername("~");

				try {
					client.sendToClient(new LogoutEvent("LOGOUT_OK"));
				} catch (IOException e) { e.printStackTrace(); }

				return;
			}

			else if (text.startsWith("LOGIN:")) {
				LoginEvent event;
				Boolean response = handleLogin(text);
				String username = extractUsername(text);
				System.out.println(response);
				if (response) {
					boolean alreadyConnected = ConnectedList.stream()
							.anyMatch(u -> u.getUsername().equals(username));
					if (!alreadyConnected) {
						ConnectedUser user = DatabaseManager.getUser(username);
						SubscribedClient sc = findClient(client);
						if (sc != null) sc.setUsername(username);
						ConnectedList.add(user);
						event = new LoginEvent("LOGIN_SUCCESS");

						// שליחת הנתונים ללקוח:
						try {
							client.sendToClient(event);
							client.sendToClient(user);                   // ConnectedUser
							Integer dbId = DatabaseManager.getUserDbId(username);
							if (dbId != null) client.sendToClient(dbId); // Integer (id מה־DB)
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						return; // יציאה מהטיפול ב־LOGIN
					} else {
						event = new LoginEvent("Already logged in");
					}
				} else {
					event = new LoginEvent("LOGIN_FAIL");
				}
				try {
					client.sendToClient(event);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// Unknown message received
				System.out.println("!! Unknown message format received: " + text);
			}

		} else if (msg instanceof ConnectedUser) {
			ConnectedUser user = (ConnectedUser) msg;
			SignUpEvent event;
			String username = user.getUsername();
			String password = user.getPassword();
			String personalId = user.getUserID();
			String creditId = user.getCreditCard();
			String role = user.getRole();

			if (DatabaseManager.userExists(username)) {
				event = new SignUpEvent("USERNAME_TAKEN");
				try {
					client.sendToClient(event);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			boolean created = DatabaseManager.createUser(username, password, personalId, creditId, role);
			if (!created) {
				event = new SignUpEvent("SIGNUP_FAILED");
				try {
					client.sendToClient(event);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			// אם זה מנוי – שנה קדימה + רישום הכנסה לאותו יום
			Integer dbId = DatabaseManager.getUserDbId(username);
			if (dbId != null && role.equals("subscription")) {
				LocalDate start = LocalDate.now();
				LocalDate end = start.plusYears(1); // בדיוק שנה
				DatabaseManager.upsertSubscription(dbId, start, end);
				DatabaseManager.addSubscriptionSale(start); // לרשימת ההכנסות לפי יום (100₪)
			}

			ConnectedUser newUser = DatabaseManager.getUser(username);
			ConnectedList.add(newUser);
			event = new SignUpEvent("SIGNUP_SUCCESS");

			try {
				client.sendToClient(event);
				client.sendToClient(newUser);
				if (dbId != null) client.sendToClient(dbId);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

		private String extractUsername (String loginMsg){
			String[] parts = loginMsg.split(":", 3);
			return parts.length >= 2 ? parts[1] : "";
		}


		public Boolean handleLogin (String text){
			String[] parts = text.split(":", 3); // Split into 3 parts max
			if (parts.length == 3) {
				String username = parts[1];
				String password = parts[2];

				//  Replace with real authentication logic
				boolean isAuthenticated = databaseManager.checkCredentials(username, password);
				return isAuthenticated;
			} else {
				return false;
			}
		}



		public SubscribedClient findClient (ConnectionToClient client){
			if (!SubscribersList.isEmpty()) {
				for (SubscribedClient subscribedClient : SubscribersList) {
					if (subscribedClient.getClient().equals(client)) {
						//SubscribersList.remove(subscribedClient);
						return subscribedClient;
					}
				}
			}
			return null;
		}


		public void sendToAllClients (Object message){
			try {
				for (SubscribedClient subscribedClient : SubscribersList) {
					subscribedClient.getClient().sendToClient(message);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}




		private void sendItem (ConnectionToClient client,int id){
			Product product = DatabaseManager.getItem(client, id);
			InitDescriptionEvent event = new InitDescriptionEvent(product);
			try {
				client.sendToClient(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	private SubscribedClient findClientByUsername(String username) {
		for (SubscribedClient sc : SubscribersList) {
			if (username.equals(sc.getUsername())) return sc;
		}
		return null;
	}


	private void notifyExpiringSubscriptions() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		List<Integer> ids = DatabaseManager.getSubscriptionsExpiringOn(tomorrow);
		if (ids.isEmpty()) return;

		// שליחת הודעה רק אם הלקוח מחובר:
		for (Integer id : ids) {
			for (SubscribedClient sc : SubscribersList) {
				// אם שמרתם username ב-SubscribedClient אפשר לתרגם ל-id דרך DB
				// כאן דוגמה פשוטה: שליחת הודעה גורפת
				try {
					sc.getClient().sendToClient("NOTIFY: Your subscription expires tomorrow");
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
	}



}
