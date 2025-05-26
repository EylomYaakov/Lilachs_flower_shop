package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.Product;

public class SimpleServer extends AbstractServer {


	private Connection dbConnection;
	private static ArrayList<SubscribedClient> SubscribersList = new ArrayList<>();

	public SimpleServer(int port) {
		super(port);
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.out.println("❌ SQLite JDBC driver not found.");
			e.printStackTrace();
		}
		connectToDatabase();
		DatabaseInitializer.initializeDatabase();

	}



	private void connectToDatabase() {
		try {
			Class.forName("org.sqlite.JDBC"); // Ensure driver is registered
			dbConnection = DriverManager.getConnection("jdbc:sqlite:plantshop.db");
			System.out.println("✅ Connected to SQLite");
		} catch (Exception e) {
			System.out.println("❌ Error initializing SQLite database: " + e.getMessage());
			e.printStackTrace();
		}
	}


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (!(msg instanceof String)) return;

		String text = (String) msg;

		if (text.startsWith("GET_CATALOG")) {
			// Client requested the full catalog
			SubscribedClient exists = findClient(client);
			if(exists == null){
				SubscribedClient connection = new SubscribedClient(client);
				SubscribersList.add(connection);
			}
			sendCatalog(client);

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
				updatePrice(client, id, newPrice);
			}

		}
		else if (text.startsWith("remove client")) {
			SubscribedClient toRemove = findClient(client);
			if (toRemove != null) {
				SubscribersList.remove(toRemove);
			}

		}
		else {
			// Unknown message received
			System.out.println("!! Unknown message format received: " + text);
		}

	}

	public SubscribedClient findClient(ConnectionToClient client) {
		if(!SubscribersList.isEmpty()){
			for(SubscribedClient subscribedClient: SubscribersList){
				if(subscribedClient.getClient().equals(client)){
					SubscribersList.remove(subscribedClient);
					return subscribedClient;
				}
			}
		}
		return null;
	}
	public void sendToAllClients(Object message) {
		try {
			for (SubscribedClient subscribedClient : SubscribersList) {
				subscribedClient.getClient().sendToClient(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void sendCatalog(ConnectionToClient client) {
		try {
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM catalog");

			List<Product> items = new ArrayList<>();
			while (rs.next()) {
				Product item = new Product(rs.getInt("id"),rs.getString("name"),rs.getString("type") ,rs.getString("description"),rs.getDouble("price"));
				items.add(item);
			}
			client.sendToClient(items);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Product getItem(ConnectionToClient client, int id) {
		try {
			PreparedStatement stmt = dbConnection.prepareStatement("SELECT * FROM catalog WHERE id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Product item = new Product(rs.getInt("id"),rs.getString("name"),rs.getString("type") ,rs.getString("description"),rs.getDouble("price"));
				return item;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private void sendItem(ConnectionToClient client, int id) {
		Product product = getItem(client, id);
		InitDescriptionEvent event = new InitDescriptionEvent(product);
		try {
			client.sendToClient(event);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updatePrice(ConnectionToClient client, int id, double price) {
		try {
			PreparedStatement stmt = dbConnection.prepareStatement("UPDATE catalog SET price = ? WHERE id = ?");
			stmt.setDouble(1, price);
			stmt.setInt(2, id);
			stmt.executeUpdate();
			Product updatedProduct = getItem(client, id);
			ChangePriceEvent event = new ChangePriceEvent(updatedProduct);
			sendToAllClients(event);

			// Send updated catalog
			sendCatalog(client);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}