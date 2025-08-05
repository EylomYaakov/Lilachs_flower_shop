package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:plantshop.db";


    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);


             /// DELETE EXISTING TABLES
            Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS complaints";
            stmt.executeUpdate(sql);
            System.out.println("✅ Complaints table deleted successfully.");

            sql = "DROP TABLE IF EXISTS Sales";

            stmt.executeUpdate(sql);
            System.out.println("✅ Sales table deleted successfully.");

            sql = "DROP TABLE IF EXISTS SaleProducts";

            stmt.executeUpdate(sql);
            System.out.println("✅ SaleProducts table deleted successfully.");
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS Orders";

            stmt.executeUpdate(sql);
            System.out.println("✅ Orders table deleted successfully.");

            sql = "DROP TABLE IF EXISTS Subscriptions";

            stmt.executeUpdate(sql);
            System.out.println("✅ Subscriptions table deleted successfully.");


            sql = "DROP TABLE IF EXISTS SubscriptionSales";

            stmt.executeUpdate(sql);
            System.out.println("✅ SubscriptionSales table deleted successfully.");


            sql = "DROP TABLE IF EXISTS OrderItems";

            stmt.executeUpdate(sql);

            System.out.println("✅ OrderItems table deleted successfully.");


            sql = "DROP TABLE IF EXISTS Users";

            stmt.executeUpdate(sql);
            System.out.println("✅ Users table deleted successfully.");


            sql = "DROP TABLE IF EXISTS catalog";

            stmt.executeUpdate(sql);

            System.out.println("✅ Catalog table deleted successfully.");
            stmt.executeUpdate(sql);





            /// CREATE NEW TABLES
            // Create the catalog table if it doesn't exist
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS catalog (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    price INTEGER NOT NULL,
                    shop TEXT NOT NULL,
                    image BLOB,
                    sale INTEGER                             
                );
            """);

            System.out.println("📦 Catalog table added successfully.");
            stmt.executeUpdate("""
            
                    CREATE TABLE IF NOT EXISTS Sales (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sale_amount INTEGER NOT NULL,
                        end_time TEXT NOT NULL
                        );
            
            """);
            System.out.println("📦 Sales table added successfully.");

            sql = """
        CREATE TABLE IF NOT EXISTS complaints (
            complaint_id INTEGER PRIMARY KEY AUTOINCREMENT,
            complaint TEXT NOT NULL,
            shop TEXT,
            order_id INTEGER,
            customer_id INTEGER,
            response TEXT,
            date TEXT,
            accepted INTEGER DEFAULT 0
        );
    """;
            stmt.executeUpdate(sql);
            System.out.println("📦complaints table added successfully. ");



            stmt.executeUpdate("""
    CREATE TABLE IF NOT EXISTS SaleProducts (
        sale_id INTEGER,
        product_id INTEGER,
        original_price DOUBLE NOT NULL,
        FOREIGN KEY (sale_id) REFERENCES Sales(id),
        FOREIGN KEY (product_id) REFERENCES catalog(id)
    );
""");
            System.out.println("📦SaleProducts table added successfully. ");




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

            System.out.println("📦Users table added successfully. ");


            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Subscriptions (
                    user_id   INTEGER PRIMARY KEY,      -- משתמש יחיד למנוי (ON CONFLICT יעבוד)
                    start_date TEXT NOT NULL,           -- yyyy-MM-dd
                    end_date   TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES Users(id)
                );
            """);
            System.out.println("📦 Subscriptions table added successfully. ");

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS SubscriptionSales (
                    sale_date TEXT NOT NULL             -- כל שורה = הרשמת מנוי (100₪)
                );
            """);
            System.out.println("📦 SubscriptionSales table added successfully. ");

            stmt.executeUpdate("""
    CREATE TABLE IF NOT EXISTS Orders (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        greeting_card TEXT,
        address TEXT,
        phone_number TEXT,
        recipient_name TEXT,
        delivery_time TEXT,
        order_date TEXT,
        total_price REAL,
        shop TEXT,
        cancelled BOOLEAN,
        complained BOOLEAN,
        refund REAL
    );
""");
            System.out.println("📦Orders table added successfully. ");


            stmt.executeUpdate("""
    CREATE TABLE IF NOT EXISTS OrderItems (
        order_id INTEGER,
        product_id INTEGER,
        quantity INTEGER,
        FOREIGN KEY(order_id) REFERENCES Orders(id),
        FOREIGN KEY(product_id) REFERENCES catalog(id)
    );
""");

            System.out.println("📦 OrderItems table added successfully. ");



            /// INITALIZE ITEMS

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

            insertCatalogWithImages(conn);




        } catch (SQLException e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
        }
    }


    private static void insertCatalogWithImages(Connection conn) throws SQLException {
        String[][] catalogData = {
                {"Flower", "Rose", "A classic red flower known for its fragrance.", "10", "Tel Aviv", "images/Rose.jpg","0"},
                {"Flower", "Tulip", "Bright and colorful spring flower.", "20", "Haifa", "images/Tulip.jpg","0"},
                {"Flower", "Lily", "Elegant white flower, often symbolic.", "10", "Tel Aviv", "images/Lily.jpg","0"},
                {"Flower", "Sunflower", "Tall yellow flower that follows the sun.", "30", "Haifa", "images/Sunflower.jpg","0"},
                {"Flower", "Orchid", "Delicate exotic flower with many varieties.", "45", "Jerusalem", "images/Orchid.jpg","0"},
                {"Plant", "Aloe Vera", "Succulent with healing properties.", "30", "all chain", "images/Aloe Vera.jpg","0"}
        };

        String sql = "INSERT INTO catalog (type, name, description, price, shop, image,sale) VALUES (?, ?, ?, ?, ?, ?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] row : catalogData) {
                String type = row[0];
                String name = row[1];
                String description = row[2];
                double price = Double.parseDouble(row[3]);
                String shop = row[4];
                String imagePath = row[5];
                int sale=Integer.parseInt(row[6]);

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
                ps.setInt(7, sale);
                ps.executeUpdate();
                System.out.println("✅ Inserted " + name + " with image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }}

    }
