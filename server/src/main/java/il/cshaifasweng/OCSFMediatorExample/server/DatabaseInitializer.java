package il.cshaifasweng.OCSFMediatorExample.server;

import java.sql.*;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:plantshop.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {



            // Create the catalog table if it doesn't exist
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS catalog (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    price INTEGER NOT NULL,
                    shop TEXT NOT NULL
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
                    role TEXT NOT NULL
                );
            """);

            System.out.println("📦 E1.");

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
                    INSERT INTO Users (Username, password, personalId, creditId, role) VALUES
                    ('Ariel', '@A1', '12345678','10','worker:manager'),
                    ('Amit', '@A1', '12345678','10', 'worker');
                    """);
                System.out.println("🌱 Users initialized with demo data.");
            } else {
                System.out.println("📦 Users already initialized.");
            }

            // Check if it's already populated
            rs = stmt.executeQuery("SELECT COUNT(*) FROM catalog");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
                    INSERT INTO catalog (type, name, description,price, shop) VALUES
                    ('Flower', 'Rose', 'A classic red flower known for its fragrance.',10, 'Tel Aviv'),
                    ('Flower', 'Tulip', 'Bright and colorful spring flower.',20 , 'Haifa'),
                    ('Flower', 'Lily', 'Elegant white flower, often symbolic.',10, 'Tel Aviv'),
                    ('Flower', 'Sunflower', 'Tall yellow flower that follows the sun.',30, 'Haifa'),
                    ('Flower', 'Orchid', 'Delicate exotic flower with many varieties.',45, 'Jerusalem'),
                    ('Plant', 'Aloe Vera', 'Succulent with healing properties.',30, 'all chain');
                """);
                System.out.println("🌱 Catalog initialized with demo data.");
            } else {
                System.out.println("📦 Catalog already initialized.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
        }
    }
}
