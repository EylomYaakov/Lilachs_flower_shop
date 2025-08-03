package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:plantshop.db";

    public static void deleteUsersTable() {
        String sql = "DROP TABLE IF EXISTS Users";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Users table deleted successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete Users table:");
            e.printStackTrace();
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);

            Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS catalog";

            stmt.executeUpdate(sql);
                System.out.println("✅ Users table deleted successfully.");


            // Create the catalog table if it doesn't exist
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS catalog (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    price INTEGER NOT NULL,
                    shop TEXT NOT NULL,
                    image BLOB                             
                );
            """);
            System.out.println("📦 E1.");

            // Create the catalog table if it doesn't exist
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    Username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    personalId TEXT NOT NULL,
                    creditId TEXT NOT NULL,
                    role TEXT NOT NULL,
                    signUpDate TEXT NOT NULL
                );
            """);

            System.out.println("📦 E1.");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Subscriptions (
                    user_id   INTEGER PRIMARY KEY,      -- משתמש יחיד למנוי (ON CONFLICT יעבוד)
                    start_date TEXT NOT NULL,           -- yyyy-MM-dd
                    end_date   TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES Users(id)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS SubscriptionSales (
                    sale_date TEXT NOT NULL             -- כל שורה = הרשמת מנוי (100₪)
                );
            """);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
                    INSERT INTO Users (Username, password, personalId, creditId, role,signUpDate) VALUES
                    ('Ariel', '@A1', '12345678','10','worker:manager','~'),
                    ('Amit', '@A1', '12345678','10', 'worker','~');
                    """);
                System.out.println("🌱 Users initialized with demo data.");
            } else {
                System.out.println("📦 Users already initialized.");
            }



/*


            // Check if it's already populated
            rs = stmt.executeQuery("SELECT COUNT(*) FROM catalog");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
        INSERT INTO catalog (type, name, description, price, shop, image) VALUES
        ('Flower', 'Rose', 'A classic red flower known for its fragrance.', 10, 'Tel Aviv', 'images/Rose.jpg'),
        ('Flower', 'Tulip', 'Bright and colorful spring flower.', 20, 'Haifa', 'images/Tulip.jpg'),
        ('Flower', 'Lily', 'Elegant white flower, often symbolic.', 10, 'Tel Aviv', 'images/Lily.jpg'),
        ('Flower', 'Sunflower', 'Tall yellow flower that follows the sun.', 30, 'Haifa', 'images/Sunflower.jpg'),
        ('Flower', 'Orchid', 'Delicate exotic flower with many varieties.', 45, 'Jerusalem', 'images/C:\\Users\\ariel_86023zc\\Desktop\\school\\u\\labs\\projectTeam7\\server\\images\\Orchid.jpg'),
        ('Plant', 'Aloe Vera', 'Succulent with healing properties.', 30, 'all chain', 'images/C:\\Users\\ariel_86023zc\\Desktop\\school\\u\\labs\\projectTeam7\\server\\images\\Aloe Vera.jpg');
    """);
                System.out.println("🌱 Catalog initialized with demo data.");
            } else {
                System.out.println("📦 Catalog already initialized.");
            }*/

            insertCatalogWithImages(conn);



        } catch (SQLException e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
        }
    }


    private static void insertCatalogWithImages(Connection conn) throws SQLException {
        String[][] catalogData = {
                {"Flower", "Rose", "A classic red flower known for its fragrance.", "10", "Tel Aviv", "images/Rose.jpg"},
                {"Flower", "Tulip", "Bright and colorful spring flower.", "20", "Haifa", "images/Tulip.jpg"},
                {"Flower", "Lily", "Elegant white flower, often symbolic.", "10", "Tel Aviv", "images/Lily.jpg"},
                {"Flower", "Sunflower", "Tall yellow flower that follows the sun.", "30", "Haifa", "images/Sunflower.jpg"},
                {"Flower", "Orchid", "Delicate exotic flower with many varieties.", "45", "Jerusalem", "images/Orchid.jpg"},
                {"Plant", "Aloe Vera", "Succulent with healing properties.", "30", "all chain", "images/Aloe Vera.jpg"}
        };

        String sql = "INSERT INTO catalog (type, name, description, price, shop, image) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : catalogData) {
                String type = row[0];
                String name = row[1];
                String description = row[2];
                double price = Double.parseDouble(row[3]);
                String shop = row[4];
                String imagePath = row[5];

                byte[] imageBytes;
                try {
                    imageBytes = Files.readAllBytes(Paths.get(imagePath));
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to read image for " + name + ": " + e.getMessage());
                    continue; // skip this item
                }

                ps.setString(1, type);
                ps.setString(2, name);
                ps.setString(3, description);
                ps.setDouble(4, price);
                ps.setString(5, shop);
                ps.setBytes(6, imageBytes);

                ps.executeUpdate();
                System.out.println("✅ Inserted " + name + " with image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }}

    }
