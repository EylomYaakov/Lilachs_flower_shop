package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;


public class SimpleServer extends AbstractServer {


	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();
	private static Map<Integer,ConnectedUser> ConnectedList = new HashMap<>() ;
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
		//DatabaseInitializer.initializeDatabase(); // initalize tables/or reset
		DatabaseManager.revertExpiredSales(); // find sales and end if needed


	}

	public static ArrayList<SubscribedClient> getAllConnectedUsers()
	{
		return SubscribersList;
	}


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		/// prints for control - server side ************************

		DatabaseManager.printAllUsers();
		System.out.println("Connected users: ");

		for (ConnectedUser cU : ConnectedList.values()) {
			System.out.println("🟢 Connected: " +
					"Username = " + cU.getUsername());

			if (cU.getRole().equals("subscription")) {
				expiredSubscriptionCheck(cU);
			}
		}

		DatabaseManager.printAllSales();
		DatabaseManager.printAllOrders();
		/// end of control prints  *************************************


		if(msg instanceof String) {/// if msg is string format


			String text = (String) msg;
			System.out.println("Message recieved from cliet: "+text);


			if (text.equals("GET_CATALOG")) {
				sendCatalogToClient(client);

			}
			/// item related ***
			else if (text.startsWith("GET_ITEM")) {
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

			} else if (text.startsWith("REMOVE_ITEM:")) {
				// Format: REMOVE_ITEM:<id>

				try {
					//extract item id
					int productId = Integer.parseInt(text.substring("REMOVE_ITEM:".length()).trim());
					System.out.println(" Request to remove product ID: " + productId);


					//remove from database
					boolean success = DatabaseManager.deleteProductById(productId);

					if (success) {
						//send to client
						System.out.println("✅ Product removed from catalog.");
						client.sendToClient("ITEM_REMOVED:" + productId); //
						RemoveProductEvent event = new RemoveProductEvent(productId);
						sendToAllClients(event);
					} else {
						System.out.println("❌ Failed to remove product with ID: " + productId);
						client.sendToClient("REMOVE_FAILED:" + productId);
					}

				} catch (NumberFormatException e) {
					System.err.println("❌ Invalid ID format in REMOVE_ITEM message: " + text);
					try {
						client.sendToClient("INVALID_REMOVE_FORMAT");
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/// client / uer related ****
			else if (text.equals("GET_USERS")) {
				try {
					List<ConnectedUser> users = DatabaseManager.getAllUsers();
					UsersListEvent event = new UsersListEvent(users);
					client.sendToClient(event);
					System.out.println("👥 Sent " + users.size() + " users to client.");
				} catch (Exception e) {
					System.err.println("❌ Failed to get users:");
					e.printStackTrace();
				}
			} else if (text.startsWith("remove client")) {// from prototype
				SubscribedClient toRemove = findClient(client);
				if (toRemove != null) {
					SubscribersList.remove(toRemove);
					if (!toRemove.getUsername().equals("~"))
						System.out.println(toRemove.getUsername());
					for (ConnectedUser cU : ConnectedList.values()) {
						if (cU.getUsername().equals(toRemove.getUsername())) {
							ConnectedList.remove(cU.getId());

							break;
						}
					}
				}

			} else if (text.startsWith("LOGIN:")) {
				// Format: LOGIN:<username>:<password>
				LoginEvent event;
				//func to ceck credentials and dup
				Boolean response = handleLogin(text);
				String username = extractUsername(text);
				System.out.println(response);
				if (response) {
					int userId = DatabaseManager.getId(username); // get id from DB
					boolean alreadyConnected = ConnectedList.containsKey(userId);
					System.out.println(alreadyConnected);
					System.out.println(ConnectedList.size());

					//if user isn't already conected (other screen)
					if (!alreadyConnected) {
						ConnectedUser user = DatabaseManager.getUserByUsername(username);
						SubscribedClient subscribedClient = findClient(client);
						subscribedClient.setUsername(username);
						SubscribersList.add(subscribedClient);
						ConnectedList.put(user != null ? user.getId() : 0, user);
						System.out.println("LOGIN_SUCCESS");
						int id = DatabaseManager.getId(user.getUsername());


						event = new LoginEvent("LOGIN_SUCCESS", user, id);
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
			}


			else if (text.startsWith("LOGOUT:")) {
				// Format: LOGOUT:<username>
				String username = extractUsername(text);
				ConnectedUser user = DatabaseManager.getUserByUsername(username);

                ConnectedUser removedUser = null;
                if (user != null) {
                    removedUser = ConnectedList.remove(user.getId());
                }
                boolean removed = (removedUser != null);


				SubscribedClient subscribedClient = findClient(client);
				if (subscribedClient != null) {
					subscribedClient.setUsername(null); // removing the username from the client server connection
				}

				LogoutEvent event;
				if (removed) {
					System.out.println("LOGOUT_OK");
					event = new LogoutEvent("LOGOUT_OK");
				} else {
					System.out.println("LOGOUT_NOT_FOUND");
					event = new LogoutEvent("LOGOUT_NOT_FOUND");
				}

				try {
					client.sendToClient(event);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/// subscription related ****
			else if (text.startsWith("SUBSCRIBED:")) {
				// Format: SUBSCRIBED:<id>

				String[] parts = text.split(":");
				if (parts.length == 2) {
					try {
						int userId = Integer.parseInt(parts[1]);
						//calc end of subscription
						LocalDate today = LocalDate.now();
						LocalDate end = today.plusYears(1);
						// ad to user and sub tables
						boolean success = DatabaseManager.upsertSubscription(userId, today, end);
						if (success) {
							System.out.println("✅ Subscription recorded for user id: " + userId);

							DatabaseManager.updateUserField(ConnectedList.get(userId).getUsername(), "role", "subscription");

						} else {
							System.out.println("❌ Failed to record subscription for user id: " + userId);
						}

					} catch (NumberFormatException e) {
						System.out.println("❌ Invalid user id in SUBSCRIBE message: " + text);
						e.printStackTrace();
					}
				}
			}
			else if(text.startsWith("GET_SUBSCRIPTION_DATES"))
			{
				try {
					List<LocalDate> subscriptionDates = DatabaseManager.getAllSubscriptionPurchaseDates();
					List<LocalDateTime> dateTimeList = convertToDateTimes(subscriptionDates);
					SubscriptionDatesListEvent event = new SubscriptionDatesListEvent(dateTimeList);

					client.sendToClient(event);
					System.out.println("📅 Sent " + subscriptionDates.size() + " subscription dates");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/// orders related ***
			else if (text.startsWith("GET_ORDERS:")) {
				// Format: GET_ORDERS:<id>

				try {//try to find orders in database

					int customerId = Integer.parseInt(text.substring("GET_ORDERS:".length()).trim());
					//save in a list
					List<Order> orders = DatabaseManager.getOrdersByCustomerId(customerId);

					System.out.println("📦 Found " + orders.size() + " orders for customer ID " + customerId);
					for (Order order : orders) {
						System.out.println("🔖 Order ID: " + order.getId());
					}
					//make object order list to send to client
					OrdersListEvent response = new OrdersListEvent(orders);
					client.sendToClient(response);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (text.startsWith("CANCEL_ORDER:")) {
				// Format: CANCEL_ORDER:<id>

				try {//try to find order in database, change canceled to true, (no option to deleete from database)
					int orderId = Integer.parseInt(text.substring("CANCEL_ORDER:".length()).trim());
					boolean success = DatabaseManager.cancelOrder(orderId);

					if (success) {
						System.out.println("🛑 Order ID " + orderId + " has been cancelled.");
						client.sendToClient("CANCEL_SUCCESS:" + orderId);
					} else {
						System.out.println("❌ Failed to cancel order ID " + orderId);
						client.sendToClient("CANCEL_FAILED:" + orderId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (text.equals("GET_ALL_ORDERS")) {
				try {
					List<Order> orders = DatabaseManager.getAllOrders();
					AllOrdersEvent event = new AllOrdersEvent(orders);

					System.out.println("📦 Sending " + orders.size() + " orders to client.");
					client.sendToClient(event);
				} catch (Exception e) {
					System.err.println("❌ Failed to fetch and send orders:");
					e.printStackTrace();
				}
			}

			/// Complaint related:
			else if (text.startsWith("GET_COMPLAINTS:")) {
				// Format: GET_COMPLAINTS:<id>
				try {
					int customerId = Integer.parseInt(text.substring("GET_COMPLAINTS:".length()).trim());

					List<Complaint> complaints = DatabaseManager.getComplaintsByCustomerId(customerId);
					ComplaintsListEvent event = new ComplaintsListEvent(complaints);

					System.out.println("📨 Sending " + complaints.size() + " complaints for customer ID " + customerId);

					client.sendToClient(event);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			else if (text.startsWith("GET_COMPLAINTS")) {
				// Format: GET_COMPLAINTS - if its GET_COMPLAINTS:<id> it would have been caught in last if statement (:)
				try {
					List<Complaint> complaints = DatabaseManager.getAllComplaints();
					ComplaintsListEvent event = new ComplaintsListEvent(complaints);

					System.out.println("📨 Sending ALL complaints (" + complaints.size() + " total)");

					client.sendToClient(event);

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (text.startsWith("GET_ALL_COMPLAINTS")) {
				try {
					List<Complaint> complaints = DatabaseManager.getAllComplaints();
					AllComplaintsEvent event = new AllComplaintsEvent(complaints);

					System.out.println("📨 Sending ALL complaints (" + complaints.size() + " total)");

					client.sendToClient(event);

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
					// Unknown message received ( but is text)
					System.out.println("!! Unknown message format received: " + text);
			}

		}
		/// obj isn't text - obj events (entities)
		else if(msg instanceof ConnectedUser) {
			/// if msg id of ConnectedUser object - signup event
			//gather details from client
			ConnectedUser user = (ConnectedUser)msg;
			SignUpEvent event;
			String username = user.getUsername();
			String password = user.getPassword();
			String personalId = user.getUserID();
			String creditId = user.getCreditCard();
			String role = user.getRole();
			String formattedDate=user.getSignUpDate();
			//check if user already exists - no sign up allowed
			boolean userExists = DatabaseManager.userExists(username);
			int id=-1;
			//response 1 - user exists
			if (userExists) {
				event = new SignUpEvent("USERNAME_TAKEN");
				System.out.println("USERNAME_TAKEN");
			}
			else {
				//create user in database
				boolean created = DatabaseManager.createUser(username, password, personalId, creditId, role,formattedDate);
				if (created) {
					ConnectedUser newUser = DatabaseManager.getUserByUsername(username);
					//ad to active list
                    if (newUser != null) {
                        ConnectedList.put(newUser.getId(),newUser);
                    }
                    id=  DatabaseManager.getId(newUser.getUsername());
					//response 2 - sign up successfull
					event = new SignUpEvent("SIGNUP_SUCCESS",newUser,id);
					System.out.println("SIGNUP_SUCCESS");
					if(role.equals("subscription"))//if signed up as subscription add to subscription table
						DatabaseManager.upsertSubscription(id, LocalDate.now(), LocalDate.now().plusYears(1));
				} else {
					//response 3- failure
					event = new SignUpEvent("SIGNUP_FAILED");
					System.out.println("SIGNUP_FAILED");

				}
			}
			try{
				client.sendToClient(event);
				client.sendToClient(id);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		else if(msg instanceof Product) {
			/// if msg is of item Product -either insert new/ alter existing
			Product product = (Product)msg;
			if(product.getId()==-1) {/// if id=-1 = new product
				System.out.println(" Received new product: " + product.getName());

				// insert product to catalog
				int newProductId = DatabaseManager.insertProduct(product);

				if (newProductId != -1) {
					System.out.println("✅ Product inserted with ID: " + newProductId);

					// add the new id given by database autoincrement
					product.setId(newProductId);
					AddProductEvent event = new AddProductEvent(product);
					sendToAllClients(event);
				} else {
					System.out.println("❌ Failed to insert product");
				}
			}
			else{

				System.out.println(" Received update for product ID " + product.getId());

				boolean success = DatabaseManager.updateProduct(product);
				if (success) {
					System.out.println(" Product updated successfully.");
					try {
						client.sendToClient(product);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("❌ Failed to update product with ID " + product.getId());
				}



			}

		}
		else if (msg instanceof Sale) {
			/// if msg is of item SALE - insert the sale, discount the items, and send to all connected clients
			Sale sale = (Sale) msg;
			System.out.println("📦 Products in this sale server side:");
			for (Product p : sale.getProducts()) {
				System.out.printf(" 🪴 ID: %d | Name: %s | Price: %.2f%n", p.getId(), p.getName(), p.getPrice());
			}
			DatabaseManager.insertSale(sale);
			System.out.println(" Sale processed and scheduled.");
			sendCatalogToClient(client);

		}

		else if(msg instanceof Order)
		{
			/// if msg is of item ORDER - insert the order to databse, no need to update client
			DatabaseManager.insertOrder((Order)msg);
			System.out.println(" Order processed and scheduled.");

		}


		else if (msg instanceof Complaint)
		{
			///  if msg is of item COMPLAINT
			Complaint complaint = (Complaint) msg;
			if(complaint.getComplaintId()==-1) {//complaint is new, add to db

				int newComplaintId = DatabaseManager.insertComplaint(complaint);
				if (newComplaintId != -1) {
					System.out.println("✅ Complaint saved for customer " + complaint.getCustomerId());
					complaint.setComplaintId(newComplaintId);
					try {
						client.sendToClient(complaint); // send back to client
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					System.out.println("❌ Complaint insert failed.");
				}
			}
			else {//complaint is existing and recieving
				DatabaseManager.updateComplaintResponse(complaint);

			}
		}
		else if (msg instanceof ChangeUserDetailsEvent) {
			ChangeUserDetailsEvent event = (ChangeUserDetailsEvent) msg;
			ConnectedUser updatedUser = event.getUser();
			String changedField = event.getChanged();

			try {
				ConnectedUser originalUser = DatabaseManager.getUserByID(updatedUser.getId());
				if (originalUser == null) {
					client.sendToClient(new ChangeUsernameEvent("User not found in database."));
					return;
				}

				// check if username is altered
				if (changedField.equalsIgnoreCase("username") &&
						!originalUser.getUsername().equals(updatedUser.getUsername())) {

					// Check if some OTHER user has this username
					boolean isUsernameTaken = DatabaseManager.userExists(updatedUser.getUsername());

					if (isUsernameTaken) {
						client.sendToClient(new ChangeUsernameEvent("Username already taken."));
						return;
					}
				}


				boolean success = DatabaseManager.updateUserDetails(updatedUser, changedField);
				if (success) {
					client.sendToClient(new ChangeUsernameEvent("SUCCESS"));
					if (changedField.equalsIgnoreCase("role")) {
						String oldRole = originalUser.getRole();
						String newRole = updatedUser.getRole();

						if (!oldRole.equalsIgnoreCase("subscription") &&
								newRole.equalsIgnoreCase("subscription")) {

							System.out.println("ℹ️ Role changed to 'subscription' but no purchase recorded.");
						}
					}

				} else {
					client.sendToClient(new ChangeUsernameEvent("Failed to update user."));
				}

			} catch (Exception e) {
				e.printStackTrace();
				try {
					client.sendToClient(new ChangeUsernameEvent("Internal server error."));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		else
			System.out.println("!! Unknown message format received: object");
	}



	public static List<LocalDateTime> convertToDateTimes(List<LocalDate> dates) {
		return dates.stream()
				.map(date -> date.atStartOfDay()) // Converts to 00:00
				.collect(Collectors.toList());
	}

	//Catalog funcs ************************************
	private void sendCatalogToClient(ConnectionToClient client)
	{
		SubscribedClient exists = findClient(client);
		if (exists == null) {
			SubscribedClient connection = new SubscribedClient(client);
			SubscribersList.add(connection);
		}
		databaseManager.sendCatalog(client);
	}


	//Client handeling ***

	private void expiredSubscriptionCheck(ConnectedUser cU) {
		String dateStr = cU.getSignUpDate();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		try {
			LocalDate signUpDate = LocalDate.parse(dateStr, formatter);
			LocalDate today = LocalDate.now();

			if (signUpDate.plusYears(1).isBefore(today)) {
				System.out.println("❌ Subscription expired for: " + cU.getUsername());
				//cancel ubcription and make normal user
				cU.setRole("chain account");
				DatabaseManager.updateUserField(cU.getUsername(), "role", "chain account");

			} else {
				System.out.println("✅ Subscription still valid for: " + cU.getUsername());
			}
		} catch (DateTimeParseException e) {
			System.err.println("⚠️ Invalid date format for user: " + cU.getUsername());
			e.printStackTrace();
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
                return databaseManager.checkCredentials(username, password);
			} else {
				return false;
			}
	}





	//Item handeling **********
	private void sendItem (ConnectionToClient client,int id){
			Product product = DatabaseManager.getItem(id);
			InitDescriptionEvent event = new InitDescriptionEvent(product);
			try {
				client.sendToClient(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}


    //Client server connction handeling***
	public SubscribedClient findClient (ConnectionToClient client){
		if (!SubscribersList.isEmpty()) {
			for (SubscribedClient subscribedClient : SubscribersList) {
				if (subscribedClient.getClient().equals(client)) {
					SubscribersList.remove(subscribedClient);
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




}
