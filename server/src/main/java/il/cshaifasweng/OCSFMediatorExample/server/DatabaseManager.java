package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.ConnectedUser;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static Connection dbConnection;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection("jdbc:sqlite:plantshop.db");
            System.out.println("✅ Connected to SQLite");
        } catch (Exception e) {
            System.out.println("❌ Error initializing SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return dbConnection;
    }

    public static void closeConnection() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("🔒 SQLite connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ConnectedUser getUser(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("personalId");
                int creditId = rs.getInt("creditId");
                String userType = rs.getString("userType");
                System.out.print(userId + " "+ creditId + " "+ userType + " ");


                return new ConnectedUser(username, userId, creditId, userType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // User not found or error occurred

    }


    public static boolean userExists(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // ✅ true if a matching row exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // or throw an exception depending on your style
        }
    }

    public static void printAllUsers() {
        String query = "SELECT * FROM Users";

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("📋 Users in database:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("Username");
                String password = rs.getString("password");
                long personalId = rs.getLong("personalId");
                long creditId = rs.getLong("creditId");
                String userType = rs.getString("userType");

                System.out.printf("🧑 ID: %d | Username: %s | Password: %s | PersonalID: %d | CreditID: %d | Type: %s%n",
                        id, username, password, personalId, creditId, userType);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to list users:");
            e.printStackTrace();
        }
    }



    public static boolean createUser(String username, String password, long personalId, long creditId, String userType) {
        String query = "INSERT INTO Users (Username, password, personalId, creditId, userType) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setLong(3, personalId);
            stmt.setLong(4, creditId);
            stmt.setString(5, userType);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean checkCredentials (String username, String password){
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // 🔐 You can hash this for production

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // true if a row was found → valid credentials

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sendCatalog(ConnectionToClient client){
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM catalog");

            List<Product> items = new ArrayList<>();
            while (rs.next()) {
                Path imagePath = Paths.get("images/" + rs.getString("name") + ".jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("price"), image);
                items.add(item);
            }
            //added only for testing the pages mechanism in the catalog - these items are not in the database and therefore exception is raised trying to get their details
            String[] names = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "q"};
            for(int i = 0; i < 100; i++) {
                Path imagePath = Paths.get("images/tulip.jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(6+i, names[i%11], names[i%11], names[i%11], 6+i, image);
                items.add(item);
            }
            client.sendToClient(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Product getItem(ConnectionToClient client, int id){
        try {
            PreparedStatement stmt = dbConnection.prepareStatement("SELECT * FROM catalog WHERE id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Path imagePath = Paths.get("images/" + rs.getString("name") + ".jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("price"), image);
                return item;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ChangePriceEvent updatePrice(ConnectionToClient client, int id, double price){
        try {
            PreparedStatement stmt = dbConnection.prepareStatement("UPDATE catalog SET price = ? WHERE id = ?");
            stmt.setDouble(1, price);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            Product updatedProduct = getItem(client, id);
            ChangePriceEvent event = new ChangePriceEvent(updatedProduct);

            // Send updated catalog
            sendCatalog(client);
            return event;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
