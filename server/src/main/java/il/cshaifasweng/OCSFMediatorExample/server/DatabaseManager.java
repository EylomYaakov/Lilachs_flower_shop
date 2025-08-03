package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.ConnectedUser;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import il.cshaifasweng.OCSFMediatorExample.entities.ProductListEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


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
                int id = rs.getInt("id");
                String userId = rs.getString("personalId");
                String creditId = rs.getString("creditId");
                String role = rs.getString("role");
                String password = rs.getString("password");
                String date = rs.getString("signUpDate");

                System.out.print("id: "+id+"not id: " +userId + " "+ creditId + " " + " ");

                return new ConnectedUser(id,username,password, userId, creditId, role,date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // User not found or error occurred

    }

    public static int getId(String username) {
        String query = "SELECT id FROM Users WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                System.err.println("⚠️ No user found with username: " + username);
                return -1; // או לזרוק חריגה, לפי הסגנון שלך
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // או לזרוק חריגה
        }
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
                String personalId = rs.getString("personalId");
                String creditId = rs.getString("creditId");
                String role = rs.getString("role");

                System.out.printf("🧑 ID: %d | Username: %s | Password: %s | PersonalID: %s | CreditID: %s | role: %s%n",
                        id, username, password, personalId, creditId, role);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to list users:");
            e.printStackTrace();
        }
    }



    public static boolean createUser(String username, String password, String personalId, String creditId, String role,String date) {
        String query = "INSERT INTO Users (Username, password, personalId, creditId, role,signUpDate) VALUES (?, ?, ?, ?, ?,?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, personalId);
            stmt.setString(4, creditId);
            stmt.setString(5, role);
            stmt.setString(6, date);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static int insertProduct(Product product) {
        String sql = "INSERT INTO catalog (type, name, description, price, shop) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, product.getType());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setString(5, product.getShop());
            ps.executeUpdate();

            // Retrieve the last inserted ID
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    product.setId(newId); // אם יש setId
                    System.out.println("✅ Product inserted with ID: " + newId);
                    return newId;
                }
            }


        } catch (SQLException e) {
            System.err.println("❌ Failed to insert product into catalog");
            e.printStackTrace();
            return -1;
        }
        return -1;
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
    public static void printFullCatalog() {
        String sql = "SELECT id, type, name, description, price, shop FROM catalog";

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("📦 Catalog contents:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String shop = rs.getString("shop");

                System.out.println("🪴 ID: " + id +
                        " | Type: " + type +
                        " | Name: " + name +
                        " | Description: " + description +
                        " | Price: " + price +
                        " | Shop: " + shop);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to print catalog");
            e.printStackTrace();
        }
    }

    public static void sendCatalog1(ConnectionToClient client){
        try {
            Statement stmt = dbConnection.createStatement();

            printFullCatalog();

            ResultSet rs = stmt.executeQuery("SELECT * FROM catalog");

            List<Product> items = new ArrayList<>();
            while (rs.next()) {
                Path imagePath = Paths.get("images/" + rs.getString("name") + ".jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("price"), image, rs.getString("shop"));
                items.add(item);
            }
            //added only for testing the pages mechanism in the catalog - these items are not in the database and therefore exception is raised trying to get their details
            String[] names = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "q"};
            for(int i = 0; i < 100; i++) {
                Path imagePath = Paths.get("images/tulip.jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(6+i, names[i%11], names[i%11], names[i%11], 6+i, image, names[i%11]);
                items.add(item);
            }
            ProductListEvent event = new ProductListEvent(items);
            client.sendToClient(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void sendCatalog(ConnectionToClient client){
        try {
            Statement stmt = dbConnection.createStatement();

            printFullCatalog();

            ResultSet rs = stmt.executeQuery("SELECT * FROM catalog");

            List<Product> items = new ArrayList<>();
            while (rs.next()) {
                byte[] image = rs.getBytes("image");  // 🔹 קרא את התמונה מה-BLOB
                Product item = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        image,
                        rs.getString("shop")
                );
                items.add(item);
            }
            //added only for testing the pages mechanism in the catalog - these items are not in the database and therefore exception is raised trying to get their details
            String[] names = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "q"};
            for(int i = 0; i < 100; i++) {
                Path imagePath = Paths.get("images/tulip.jpg");
                byte[] image = Files.readAllBytes(imagePath);
                Product item = new Product(6+i, names[i%11], names[i%11], names[i%11], 6+i, image, names[i%11]);
                items.add(item);
            }
            ProductListEvent event = new ProductListEvent(items);
            client.sendToClient(event);
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
                Product item = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getDouble("price"), image, rs.getString("shop"));
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
    public static void updateUserField(String username, String fieldName, String newValue) {
        String sql = "UPDATE Users SET " + fieldName + " = ? WHERE username = ?";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, newValue);
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("✅ Updated " + fieldName + " for user " + username);
        } catch (SQLException e) {
            System.err.println("❌ Failed to update " + fieldName + " for user " + username);
            e.printStackTrace();
        }
    }

    public static Integer getUserDbId(String username) {
        String sql = "SELECT id FROM Users WHERE username = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static boolean upsertSubscription(int userId, LocalDate start, LocalDate end) {
        String sql = "INSERT INTO Subscriptions(user_id,start_date,end_date) VALUES(?,?,?) " +
                "ON CONFLICT(user_id) DO UPDATE SET start_date=excluded.start_date, end_date=excluded.end_date";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, start.format(ISO));
            ps.setString(3, end.format(ISO));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static void addSubscriptionSale(LocalDate date) {
        String sql = "INSERT INTO SubscriptionSales(sale_date) VALUES(?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, date.format(ISO));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Integer> getSubscriptionsExpiringOn(LocalDate date) {
        String sql = "SELECT user_id FROM Subscriptions WHERE end_date = ?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, date.format(ISO));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("user_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public static void printAllSubscriptionsAndSales() {
        System.out.println("📋 Subscriptions:");
        String subscriptionQuery = "SELECT user_id, start_date, end_date FROM Subscriptions";

        try (PreparedStatement ps = dbConnection.prepareStatement(subscriptionQuery);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String start = rs.getString("start_date");
                String end = rs.getString("end_date");

                System.out.printf("🧑 User ID: %d | Start: %s | End: %s%n", userId, start, end);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error reading Subscriptions table");
            e.printStackTrace();
        }

        System.out.println("\n📊 Subscription Sales:");
        String salesQuery = "SELECT sale_date FROM SubscriptionSales";

        try (PreparedStatement ps = dbConnection.prepareStatement(salesQuery);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getString("sale_date");
                System.out.println("📅 Sale Date: " + date);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error reading SubscriptionSales table");
            e.printStackTrace();
        }
    }


}
