package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {
    private static Connection dbConnection;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

                System.out.print("id: " + id + "not id: " + userId + " " + creditId + " " + " ");

                return new ConnectedUser(id, username, password, userId, creditId, role, date);
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


    public static boolean createUser(String username, String password, String personalId, String creditId, String role, String date) {
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
        String sql = "INSERT INTO catalog (type, name, description, price, shop, image) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, product.getType());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setString(5, product.getShop());
            ps.setBytes(6, product.getImage()); // הוספת התמונה כאן

            ps.executeUpdate();

            // Retrieve the last inserted ID
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    product.setId(newId); // רק אם יש setId
                    System.out.println("✅ Product inserted with ID: " + newId);
                    return newId;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert product into catalog");
            e.printStackTrace();
        }

        return -1;
    }

    public static boolean updateProduct(Product product) {
        String sql = "UPDATE catalog SET type = ?, name = ?, description = ?, price = ?, shop = ?, image = ? WHERE id = ?";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, product.getType());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrice());
            ps.setString(5, product.getShop());
            ps.setBytes(6, product.getImage()); // assuming getImage returns byte[]
            ps.setInt(7, product.getId());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Failed to update product with ID: " + product.getId());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteProductById(int id) {
        String sql = "DELETE FROM catalog WHERE id = ?";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete product with ID: " + id);
            e.printStackTrace();
            return false;
        }

    }


    public static void insertSale(Sale sale) {
        String sql = "INSERT INTO sales (sale_Amount, end_time) VALUES (?, ?)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setInt(1, sale.getSaleAmount());
            ps.setString(2, sale.getEnd().toString());
            ps.executeUpdate();

            // Get the generated sale ID
            int saleId = -1;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
            }

            System.out.println("🎉 Sale inserted with ID: " + saleId);

            // INSERT product links and update prices
            String insertSaleProductSql = "INSERT INTO SaleProducts (sale_id, product_id, original_price) VALUES (?, ?, ?)";
            PreparedStatement insertSaleProductStmt = dbConnection.prepareStatement(insertSaleProductSql);
            System.out.println("📦 Products in this sale:");
            for (Product p : sale.getProducts()) {
                System.out.printf("   🪴 ID: %d | Name: %s | Price: %.2f%n", p.getId(), p.getName(), p.getPrice());
            }

            for (Product p : sale.getProducts()) {
                double originalPrice = p.getPrice();
                double discountedPrice = originalPrice * (100 - sale.getSaleAmount()) / 100.0;

                // 1️⃣ שמירת המחיר המקורי בטבלת הקישור
                insertSaleProductStmt.setInt(1, saleId);
                insertSaleProductStmt.setInt(2, p.getId());
                insertSaleProductStmt.setDouble(3, originalPrice);
                insertSaleProductStmt.executeUpdate();

                // 2️⃣ עדכון המחיר בהנחה ב־catalog
                Product discounted = new Product(
                        p.getId(),
                        p.getName(),
                        p.getType(),
                        p.getDescription(),
                        discountedPrice,
                        p.getImage(),
                        p.getShop()
                );

                boolean success = updateProduct(discounted);
                if (success) {
                    System.out.println("✅ Updated product " + p.getName() + " with discount");
                } else {
                    System.out.println("❌ Failed to update product " + p.getName());
                }
            }

            // סיום
            insertSaleProductStmt.close();

            // תזמון סיום המבצע
            scheduleSaleReversion(saleId, sale.getEnd());

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert sale");
            e.printStackTrace();
        }
    }


   /* public static void revertSale(int saleId) {
        String productIdsSql = "SELECT product_id FROM sale_products WHERE sale_id = ?";
        String deleteLinkSql = "DELETE FROM sale_products WHERE sale_id = ?";

        try (PreparedStatement ps = dbConnection.prepareStatement(productIdsSql)) {
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int productId = rs.getInt("product_id");

                Product original = getOriginalProduct(productId); // נניח שאת שומרת את original_price
                if (original != null) {
                    updateProduct(original);
                    System.out.println("↩️ Reverted price for product ID: " + productId);
                }
            }

            // מחק קישורים מהטבלה
            try (PreparedStatement deleteStmt = dbConnection.prepareStatement(deleteLinkSql)) {
                deleteStmt.setInt(1, saleId);
                deleteStmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to revert sale " + saleId);
            e.printStackTrace();
        }
    }*/

    public static void printAllSales() {
        String salesSql = "SELECT id, sale_Amount, end_time FROM sales";
        String productsSql = "SELECT * FROM catalog c " +
                "JOIN SaleProducts sp ON c.id = sp.product_id " +
                "WHERE sp.sale_id = ?";

        try (PreparedStatement productStmt = dbConnection.prepareStatement(productsSql);
             Statement saleStmt = dbConnection.createStatement();
             ResultSet saleRs = saleStmt.executeQuery(salesSql)) {

            while (saleRs.next()) {
                int saleId = saleRs.getInt("id");
                int saleAmount = saleRs.getInt("sale_Amount");
                String endTime = saleRs.getString("end_time");

                System.out.println("🔖 Sale ID: " + saleId);
                System.out.println("   Discount: " + saleAmount + "%");
                System.out.println("   Ends at: " + endTime);
                System.out.println("   Products in sale:");

                // עכשיו נשלוף את המוצרים של המבצע הזה
                productStmt.setInt(1, saleId);
                try (ResultSet productRs = productStmt.executeQuery()) {
                    boolean hasProducts = false;
                    while (productRs.next()) {
                        hasProducts = true;
                        int productId = productRs.getInt("id");
                        String name = productRs.getString("name");
                        double price = productRs.getDouble("price");
                        System.out.println("      🪴 ID: " + productId + " | Name: " + name + " | Price: " + price);
                    }

                    if (!hasProducts) {
                        System.out.println("      (no products in sale)");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to print sales");
            e.printStackTrace();
        }
    }


    private static void scheduleSaleReversion(int saleId, LocalDateTime endTime) {
        long delayMillis = Duration.between(LocalDateTime.now(), endTime).toMillis();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            try {
                PreparedStatement selectStmt = dbConnection.prepareStatement(
                        "SELECT product_id, original_price FROM SaleProducts WHERE sale_id = ?"
                );
                selectStmt.setInt(1, saleId);
                ResultSet rs = selectStmt.executeQuery();

                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    double originalPrice = rs.getDouble("original_price");

                    PreparedStatement updateStmt = dbConnection.prepareStatement(
                            "UPDATE catalog SET price = ? WHERE id = ?"
                    );
                    updateStmt.setDouble(1, originalPrice);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                }

                rs.close();
                selectStmt.close();

                System.out.println("🔁 Sale ID " + saleId + " ended, prices restored.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            scheduler.shutdown();
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public static void insertSale1(Sale sale) {
        String sql = "INSERT INTO sales (sale_Amount, end_time) VALUES (?, ?)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sale.getSaleAmount());
            ps.setString(2, sale.getEnd().toString()); // אפשר גם לשמור בפורמט Timestamp אם רוצים

            ps.executeUpdate();

            // Get the generated sale ID (SQLite-compatible way)
            int saleId = -1;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
            }


            System.out.println("🎉 Sale inserted with ID: " + saleId);

            // Apply sale to each product
            for (Product p : sale.getProducts()) {
                double originalPrice = p.getPrice();
                double discountedPrice = originalPrice * (100 - sale.getSaleAmount()) / 100.0;

                // Create a copy of the product with updated price
                Product discounted = new Product(
                        p.getId(),
                        p.getName(),
                        p.getType(),
                        p.getDescription(),
                        discountedPrice,
                        p.getImage(),
                        p.getShop()
                );

                boolean success = updateProduct(discounted);
                if (success) {
                    System.out.println("✅ Updated product " + p.getName() + " with discount");
                } else {
                    System.out.println("❌ Failed to update product " + p.getName());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert sale");
            e.printStackTrace();
        }
    }

    public static List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();

        String orderSql = "SELECT * FROM orders WHERE customer_id = ?";
        String itemsSql = "SELECT product_id, quantity FROM OrderItems WHERE order_id = ?";

        try (PreparedStatement orderStmt = dbConnection.prepareStatement(orderSql)) {
            orderStmt.setInt(1, customerId);
            ResultSet orderRs = orderStmt.executeQuery();

            while (orderRs.next()) {
                int orderId = orderRs.getInt("id");
                String greetingCard = orderRs.getString("greeting_card");
                String address = orderRs.getString("address");
                String phone = orderRs.getString("phone_number");
                String name = orderRs.getString("recipient_name");
                LocalDateTime deliveryTime = LocalDateTime.parse(orderRs.getString("delivery_time"));
                LocalDate orderDate = LocalDate.parse(orderRs.getString("order_date"));
                double price = orderRs.getDouble("total_price");
                boolean cancelled = orderRs.getBoolean("cancelled");
                boolean complained = orderRs.getBoolean("complained");
                double refund = orderRs.getDouble("refund");

                // items in order
                Map<BaseProduct, Integer> products = new HashMap<>();
                try (PreparedStatement itemsStmt = dbConnection.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, orderId);
                    ResultSet itemsRs = itemsStmt.executeQuery();

                    while (itemsRs.next()) {
                        int productId = itemsRs.getInt("product_id");
                        int quantity = itemsRs.getInt("quantity");

                        Product product = getItem(productId);
                        if (product != null) {
                            products.put(product, quantity);
                        }
                    }
                }

                Order order = new Order(products, greetingCard, address, phone, name,
                        deliveryTime, orderDate, price, customerId);
                order.setId(orderId);
                order.setCancelled(cancelled);
                order.setComplained(complained);
                order.setRefund(refund);

                orders.add(order);
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to retrieve orders for customer ID: " + customerId);
            e.printStackTrace();
        }

        return orders;
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
    public static Product getItem( int id){
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
            Product updatedProduct = getItem(id);
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


    public static int insertOrder(Order order) {
        String insertOrderSql = """
        INSERT INTO Orders (
            customer_id, greeting_card, address, phone_number, recipient_name,
            delivery_time, order_date, total_price, shop, cancelled, complained, refund
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    """;

        String insertItemSql = """
        INSERT INTO OrderItems (order_id, product_id, quantity)
        VALUES (?, ?, ?);
    """;

        try (PreparedStatement orderStmt = dbConnection.prepareStatement(insertOrderSql);
             PreparedStatement itemStmt = dbConnection.prepareStatement(insertItemSql)) {

            // add the orderi itself
            orderStmt.setInt(1, order.getCustomerId());
            orderStmt.setString(2, order.getGreetingCard());
            orderStmt.setString(3, order.getAddress());
            orderStmt.setString(4, order.getPhoneNumber());
            orderStmt.setString(5, order.getName());
            orderStmt.setString(6, order.getDeliveryTime().toString());
            orderStmt.setString(7, order.getOrderDate().toString());
            orderStmt.setDouble(8, order.getPrice());
            orderStmt.setString(9, order.getShop());
            orderStmt.setBoolean(10, order.isCancelled());
            orderStmt.setBoolean(11, order.isComplained());
            orderStmt.setDouble(12, order.getRefund());

            orderStmt.executeUpdate();

            // get the id of the new order
            int orderId = -1;
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid();")) {
                if (rs.next()) {
                    orderId = rs.getInt(1);
                    order.setId(orderId);
                }
            }

            // add items to order
            for (Map.Entry<BaseProduct, Integer> entry : order.getProducts().entrySet()) {
                if (!(entry.getKey() instanceof Product)) continue;

                Product product = (Product) entry.getKey();
                int productId = product.getId();
                int quantity = entry.getValue();

                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.executeUpdate();
            }

            System.out.println("🧾 Order inserted with ID: " + orderId);
            return orderId;

        } catch (SQLException e) {
            System.err.println("❌ Failed to insert order");
            e.printStackTrace();
            return -1;
        }
    }


    public static void printAllOrders() {
        String ordersSql = "SELECT * FROM Orders";
        String itemsSql = "SELECT product_id, quantity FROM OrderItems WHERE order_id = ?";

        try (Statement orderStmt = dbConnection.createStatement();
             ResultSet ordersRs = orderStmt.executeQuery(ordersSql)) {

            while (ordersRs.next()) {
                int orderId = ordersRs.getInt("id");
                int customerId = ordersRs.getInt("customer_id");
                String recipientName = ordersRs.getString("recipient_name");
                String address = ordersRs.getString("address");
                String phone = ordersRs.getString("phone_number");
                String greeting = ordersRs.getString("greeting_card");
                String delivery = ordersRs.getString("delivery_time");
                String orderDate = ordersRs.getString("order_date");
                String shop = ordersRs.getString("shop");
                double total = ordersRs.getDouble("total_price");
                boolean cancelled = ordersRs.getBoolean("cancelled");
                boolean complained = ordersRs.getBoolean("complained");
                double refund = ordersRs.getDouble("refund");

                System.out.println("🧾 Order ID: " + orderId);
                System.out.println("   Customer ID: " + customerId);
                System.out.println("   Recipient: " + recipientName);
                System.out.println("   Address: " + address);
                System.out.println("   Phone: " + phone);
                System.out.println("   Greeting: " + greeting);
                System.out.println("   Delivery Time: " + delivery);
                System.out.println("   Order Date: " + orderDate);
                System.out.println("   Shop: " + shop);
                System.out.println("   Total Price: " + total);
                System.out.println("   Cancelled: " + cancelled);
                System.out.println("   Complained: " + complained);
                System.out.println("   Refund: " + refund);

                // show the items
                try (PreparedStatement itemStmt = dbConnection.prepareStatement(itemsSql)) {
                    itemStmt.setInt(1, orderId);
                    try (ResultSet itemsRs = itemStmt.executeQuery()) {
                        System.out.println("   📦 Products:");
                        while (itemsRs.next()) {
                            int productId = itemsRs.getInt("product_id");
                            int quantity = itemsRs.getInt("quantity");
                            System.out.println("      🔸 Product ID: " + productId + " | Quantity: " + quantity);
                        }
                    }
                }

                System.out.println();
            }

        } catch (SQLException e) {
            System.err.println("❌ Failed to print orders");
            e.printStackTrace();
        }
    }

    public static Order getOrderById(int orderId) {
        String orderSql = "SELECT * FROM orders WHERE id = ?";
        String itemsSql = "SELECT product_id, quantity FROM OrderItems WHERE order_id = ?";

        try (PreparedStatement orderStmt = dbConnection.prepareStatement(orderSql)) {
            orderStmt.setInt(1, orderId);
            ResultSet orderRs = orderStmt.executeQuery();

            if (!orderRs.next()) {
                return null; // לא קיימת הזמנה עם מזהה כזה
            }

            // קריאה לפרטי ההזמנה
            String greetingCard = orderRs.getString("greeting_card");
            String address = orderRs.getString("address");
            String phone = orderRs.getString("phone_number");
            String name = orderRs.getString("customer_name");
            LocalDateTime deliveryTime = LocalDateTime.parse(orderRs.getString("delivery_time"));
            LocalDate orderDate = LocalDate.parse(orderRs.getString("order_date"));
            double price = orderRs.getDouble("price");
            int customerId = orderRs.getInt("customer_id");
            boolean cancelled = orderRs.getBoolean("cancelled");
            boolean complained = orderRs.getBoolean("complained");
            double refund = orderRs.getDouble("refund");

            // קריאה לפרטי הפריטים
            Map<BaseProduct, Integer> products = new HashMap<>();
            try (PreparedStatement itemsStmt = dbConnection.prepareStatement(itemsSql)) {
                itemsStmt.setInt(1, orderId);
                ResultSet itemsRs = itemsStmt.executeQuery();

                while (itemsRs.next()) {
                    int productId = itemsRs.getInt("product_id");
                    int quantity = itemsRs.getInt("quantity");

                    Product product = getItem(productId); // שליפת פריט לפי מזהה
                    if (product != null) {
                        products.put(product, quantity);
                    }
                }
            }

            // create object (temp)
            Order order = new Order(products, greetingCard, address, phone, name,
                    deliveryTime, orderDate, price, customerId);
            order.setId(orderId);
            order.setRefund(refund);
            order.setCancelled(cancelled);
            order.setComplained(complained);

            return order;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



}
