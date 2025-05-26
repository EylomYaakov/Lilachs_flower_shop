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
                    price INTEGER NOT NULL  
                );
            """);

            // Check if it's already populated
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM catalog");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate("""
                    INSERT INTO catalog (type, name, description,price) VALUES
                    ('Flower', 'Rose', 'A classic red flower known for its fragrance.',10),
                    ('Flower', 'Tulip', 'Bright and colorful spring flower.',20),
                    ('Flower', 'Lily', 'Elegant white flower, often symbolic.',10),
                    ('Flower', 'Sunflower', 'Tall yellow flower that follows the sun.',30),
                    ('Flower', 'Orchid', 'Delicate exotic flower with many varieties.',45),
                    ('Plant', 'Aloe Vera', 'Succulent with healing properties.',30),
                    ('Plant', 'Basil', 'Fragrant herb used in cooking.',20),
                    ('Plant', 'Fern', 'Lush green plant ideal for indoors.',40),
                    ('Plant', 'Cactus', 'Spiky desert plant that needs little water.',9),
                    ('Plant', 'Lavender', 'Aromatic plant with purple flowers.',20);
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
