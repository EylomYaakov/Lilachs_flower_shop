package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.SubscribedClient;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleServer extends AbstractServer {


	private Connection dbConnection;

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

		} else {
			// Unknown message received
			System.out.println("!! Unknown message format received: " + text);
		}
	}

/*
	private void sendWarningToClient(ConnectionToClient client) {
		try {
			Warning warning = new Warning("Warning from server!");
			client.sendToClient(warning);
			System.out.printf(">> Sent warning to client at %s%n", client.getInetAddress().getHostAddress());
		} catch (IOException e) {
			System.err.println("!! Failed to send warning.");
			e.printStackTrace();
		}
	}

	private void registerNewClient(ConnectionToClient client) {
		connectedClients.add(new SubscribedClient(client));
		try {
			if (playersJoined == 0) {
				selectedFirstPlayer = new Random().nextInt(2);
				client.sendToClient("client added successfully with sign " + signs[selectedFirstPlayer]);
				System.out.println(">> First player assigned: " + signs[selectedFirstPlayer]);
			} else if (playersJoined == 1) {
				client.sendToClient("client added successfully with sign " + signs[1 - selectedFirstPlayer]);
				System.out.println(">> Second player assigned: " + signs[1 - selectedFirstPlayer]);
			}
			playersJoined++;

			if (playersJoined == 2) {
				System.out.println(">> Game ready: Two players connected.");
				broadcastMessage("all clients are connected");
				broadcastMessage(moveCount + "move");
			}
		} catch (IOException e) {
			System.err.println("!! Error assigning player sign.");
			e.printStackTrace();
		}
	}


	private void unregisterClient(ConnectionToClient client) {
		moveCount=0;

		try {
			// Close connection
			client.close();
		} catch (IOException e) {
			System.err.println("!! Error while closing client connection.");
			e.printStackTrace();
		}

		// Remove from list
		connectedClients.removeIf(sc -> sc.getClient().equals(client));
		System.out.println(">> Client disconnected and removed.");

		// Decrease playersJoined to allow re-join
		if (playersJoined > 0) {
			playersJoined--;
		}


	}

	private void handleGameOver() {
		System.out.println(">> Game ended. Resetting for next match.");
		moveCount = 0;
		playersJoined = 0;
		connectedClients.clear();
		broadcastMessage("new game");
	}

	private void processMoveMessage(String msg) {
		moveCount++;
		System.out.printf(">> Move #%d received: %s%n", moveCount, msg);
		broadcastMessage(msg + moveCount);
	}



	private void broadcastMessage(String msg) {
		for (SubscribedClient client : connectedClients) {
			try {
				client.getClient().sendToClient(msg);
			} catch (IOException e) {
				System.err.println("!! Failed to deliver message to a client.");
				e.printStackTrace();
			}
		}
	}
}
*/

private void sendCatalog(ConnectionToClient client) {
	try {
		Statement stmt = dbConnection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM catalog");

		List<String> items = new ArrayList<>();
		while (rs.next()) {
			String line = rs.getInt("id") + "," +
					rs.getString("name") + "," +
					rs.getString("type") + "," +
					rs.getDouble("price");
			items.add(line);
		}
		client.sendToClient(items);
	} catch (Exception e) {
		e.printStackTrace();
	}
}

private void sendItem(ConnectionToClient client, int id) {
	try {
		PreparedStatement stmt = dbConnection.prepareStatement("SELECT * FROM catalog WHERE id = ?");
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			String item = rs.getInt("id") + "," +
					rs.getString("name") + "," +
					rs.getString("type") + "," +
					rs.getDouble("price");
			client.sendToClient(item);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}

private void updatePrice(ConnectionToClient client, int id, double price) {
	try {
		PreparedStatement stmt = dbConnection.prepareStatement("UPDATE catalog SET price = ? WHERE id = ?");
		stmt.setDouble(1, price);
		stmt.setInt(2, id);
		stmt.executeUpdate();
		client.sendToClient("PRICE_UPDATED");

		// Send updated catalog
		sendCatalog(client);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}