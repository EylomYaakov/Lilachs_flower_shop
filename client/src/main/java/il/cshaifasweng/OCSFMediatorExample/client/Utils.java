package il.cshaifasweng.OCSFMediatorExample.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import il.cshaifasweng.OCSFMediatorExample.entities.BaseProduct;
import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

public class Utils {
    public static boolean isValidImagePath(String imagePath) {
        Path path = Paths.get(imagePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return false;
        }
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            return img != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static double getPrice(String price){
        double priceDouble = 0;
        try{
            priceDouble = Double.parseDouble(price);
            if(priceDouble <= 0){
                return -1;
            }
        }
        catch(NumberFormatException e){
            return -1;
        }
        return priceDouble;
    }

    public static int getDiscount(String discount){
        int discountInt = 0;
        try{
            discountInt = Integer.parseInt(discount);
            if(discountInt <= 0 || discountInt >= 100){
                return -1;
            }
        }
        catch(NumberFormatException e){
            return -1;
        }
        return discountInt;
    }

    public static void setStyleAllButtons(Button[] buttons, String style){
        for(Button button : buttons){
            Platform.runLater(()->button.setStyle(style));
        }
    }

    public static boolean isValidPassword(String password){
        boolean captial = false, lowercase = false, number = false;
        for(int i=0; i<password.length(); i++){
            if(Character.isUpperCase(password.charAt(i))){
                captial = true;
            }
            else if(Character.isLowerCase(password.charAt(i))){
                lowercase = true;
            }
            else if(Character.isDigit(password.charAt(i))){
                number = true;
            }
        }
        return captial && lowercase && number && password.length() >= 8;
    }

    public static void initReportOptions(DatePicker startDate, DatePicker endDate, ComboBox<String> reportTypes, ComboBox<String> shopsList, Label shopLabel){
        startDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable all future dates
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #dddddd;");
                }
            }
        });
        endDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable all future dates
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #dddddd;");
                }
            }
        });
        Platform.runLater(()->reportTypes.getItems().addAll("income", "orders", "complaints"));
        initShopsFilter(shopsList);
        Platform.runLater(()->shopLabel.setVisible(shopsList.isVisible()));
    }

    public static void sendReportMessage(String type){
        if(type.equals("income")){
            try{
                SimpleClient.getClient().sendToServer("GET_SUBSCRIPTION_DATES");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        String message = "GET_ALL_";
        if(type.equals("income") || type.equals("orders")){
            message += "ORDERS";
        }
        else{
            message += "COMPLAINTS";
        }
        try {
            SimpleClient.getClient().sendToServer(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void incomeReport(List<Order> orders, BarChart<String, Number> histogram, LocalDate startDate, LocalDate endDate, String shop, List<LocalDate> subscriptionDates){
        Map<LocalDate, Double> data = new LinkedHashMap<>();
        for(LocalDate begin = startDate; begin.isBefore(endDate.plusDays(1)); begin = begin.plusDays(1)) {
            data.put(begin, 0.0);
        }
        for(Order order : orders) {
            data.put(order.getOrderDate(), data.get(order.getOrderDate()) + order.getPrice());
        }
        for(LocalDate date : subscriptionDates){
            data.put(date, data.get(date) + 100);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Double> entry : data.entrySet()) {
            String formattedDate = entry.getKey().format(formatter);
            series.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
        }
        Platform.runLater(()->histogram.getData().add(series));
        Platform.runLater(()->histogram.setTitle("income " + title(startDate, endDate, shop)));
        Platform.runLater(()->histogram.legendVisibleProperty().set(false));
    }


    public static void ordersReport(List<Order> orders, BarChart<String, Number> histogram, LocalDate startDate, LocalDate endDate, String shop){
        Set<String> types = new HashSet<>();
        for(Order order : orders) {
            for(BaseProduct product: order.getProducts().keySet()){
                types.add(product.type);
            }
        }
        Map<String, Integer> data = new HashMap<>();
        for(String type : types){
            data.put(type, 0);
        }
        for(Order order : orders){
            for(Map.Entry<BaseProduct, Integer> entry: order.getProducts().entrySet()){
                data.put(entry.getKey().type, data.get(entry.getKey().type) + entry.getValue());
            }
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        Platform.runLater(()->histogram.getData().add(series));
        Platform.runLater(()->histogram.setTitle("orders " + title(startDate, endDate, shop)));
        Platform.runLater(()->histogram.legendVisibleProperty().set(false));
    }

    public static void complaintsReport(List<Complaint> complaints, BarChart<String, Number> histogram, LocalDate startDate, LocalDate endDate, String shop){
        Map<LocalDate, Integer> accepted = new LinkedHashMap<>();
        Map<LocalDate, Integer> rejected = new LinkedHashMap<>();
        for(LocalDate begin = startDate; begin.isBefore(endDate.plusDays(1)); begin = begin.plusDays(1)) {
            accepted.put(begin, 0);
            rejected.put(begin, 0);
        }
        for(Complaint complaint : complaints) {
            LocalDate date = complaint.getDate();
            if(complaint.getAccepted()){
                accepted.put(date, accepted.get(date) + 1);
            }
            else{
                rejected.put(date, rejected.get(date) + 1);
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        XYChart.Series<String, Number> seriesAccepted = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Integer> entry : accepted.entrySet()) {
            String formattedDate = entry.getKey().format(formatter);
            seriesAccepted.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
        }
        XYChart.Series<String, Number> seriesRejected = new XYChart.Series<>();
        for (Map.Entry<LocalDate, Integer> entry : rejected.entrySet()) {
            String formattedDate = entry.getKey().format(formatter);
            seriesRejected.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
        }
        seriesAccepted.setName("Accepted");
        seriesRejected.setName("Rejected");
        Platform.runLater(()->histogram.getData().add(seriesAccepted));
        Platform.runLater(()->histogram.getData().add(seriesRejected));
        Platform.runLater(()->histogram.setTitle("complaints " + title(startDate, endDate, shop)));
    }


    private static String title(LocalDate startDate, LocalDate endDate, String shop){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "Report for " + shop + ", " + formatter.format(startDate) + " - " + formatter.format(endDate);
    }


    public static void initShopsFilter(ComboBox<String> shopsList){
        Platform.runLater(()->shopsList.getItems().add("all chain"));
        List<String> shops = CatalogController.getShops();
        for(String shop : shops) {
            Platform.runLater(()->shopsList.getItems().add(shop));
        }
        String shop = SimpleClient.getUser().getShop();
        if(!shop.equals("all chain")){
            Platform.runLater(()->shopsList.getSelectionModel().select(shop));
            Platform.runLater(()->shopsList.setVisible(false));
        }
    }

}
