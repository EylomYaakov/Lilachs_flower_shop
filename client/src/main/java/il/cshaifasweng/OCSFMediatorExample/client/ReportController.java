package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;

import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.*;

public class ReportController {

    @FXML
    private Button compareButton;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label endLabel;

    @FXML
    private BarChart<String, Number> reportHistogram;

    @FXML
    private Label shopLabel;

    @FXML
    private ComboBox<String> shopsList;


    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Label startLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private ComboBox<String> typesList;

    private String reportType;
    private String shop;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean compare = false;

    @FXML
    public void initialize() {
        Utils.initReportOptions(startDatePicker, endDatePicker, typesList, shopsList);
    }

    @Subscribe
    public void getOrders(AllOrdersEvent event) {
        List<Order> orders = event.getOrders();
        List<Order> ordersInRange = new ArrayList<>();
        for(Order order : orders) {
            LocalDate orderDate = order.getOrderDate();
            if(orderDate.isAfter(endDate) || orderDate.isBefore(startDate)) {
                continue;
            }
            ordersInRange.add(order);

        }
        if(compare) {
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("compareReports.fxml"));
                Parent root = loader.load();
                CompareReportsController controller = loader.getController();
                controller.setHistogram1(reportHistogram);
                callReportOrderList(ordersInRange, controller.getHistogram2(), reportType, startDate, endDate, shop);
                Scene scene = new Scene(root);
                Stage stage = (Stage) reportHistogram.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
                return;
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        callReportOrderList(ordersInRange, reportHistogram, reportType, startDate, endDate, shop);
    }


    @Subscribe
    public void getComplaints(AllComplaintsEvent event) {
        List<Complaint> complaints = event.getComplaints();
        List<Complaint> complaintsInRange = new ArrayList<>();
        for(Complaint complaint : complaints) {
            LocalDate orderDate = complaint.getDate();
            if(orderDate.isAfter(endDate) || orderDate.isBefore(startDate)) {
                continue;
            }
            complaintsInRange.add(complaint);
        }
        if(compare){
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("compareReports.fxml"));
                Parent root = loader.load();
                CompareReportsController controller = loader.getController();
                controller.setHistogram1(reportHistogram);
                Utils.complaintsReport(complaintsInRange, controller.getHistogram2(), startDate, endDate, shop);
                Scene scene = new Scene(root);
                Stage stage = (Stage) reportHistogram.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
                return;
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
       Utils.complaintsReport(complaintsInRange, reportHistogram, startDate, endDate, shop);
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;

        Map map  = new HashMap();
        Order order1 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), -1);
        Order order2 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), -1);
        Order order3 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(2), -1);
        Order order4 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(3), -1);
        Order order5 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), -1);
        Order order6 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), -1);
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);
        orders.add(order6);
        AllOrdersEvent allOrdersEventIncome = new AllOrdersEvent(orders);

        Product product1 = new Product(1, "a", "plant", "a", 1, null);
        Product product2 = new Product(2, "b", "plant", "b", 1, null);
        Product product3 = new Product(3, "c", "flower", "c", 1, null);
        Product product4 = new Product(4, "d", "flower", "d", 1, null);
        Product product5 = new Product(5, "e", "flower", "e", 1, null);
        Product product6 = new Product(6, "f", "bouquet", "f", 1, null);
        Product product7 = new Product(7, "g", "bouquet", "g", 1, null);

        Map<BaseProduct, Integer> products1 = new HashMap();
        products1.put(product1, 2);
        products1.put(product3, 3);
        Map<BaseProduct, Integer> products2 = new HashMap();
        products2.put(product3, 3);
        products2.put(product4, 4);
        products2.put(product5, 5);
        Map<BaseProduct, Integer> products3 = new HashMap();
        products3.put(product6, 4);
        products3.put(product7, 6);
        products3.put(product2, 3);
        Map<BaseProduct, Integer> products4 = new HashMap();
        products4.put(product4, 4);

        Order order7 = new Order(products1, "a", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 1);
        Order order8 = new Order(products2, "b", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 2);
        Order order9 = new Order(products3, "c", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 3);
        Order order10 = new Order(products4, "d", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 4);

        List<Order> orders2 = new ArrayList<>();
        orders.add(order7);
        orders.add(order8);
        orders.add(order9);
        orders.add(order10);
        AllOrdersEvent allOrdersEventOrders = new AllOrdersEvent(orders);

        Complaint complaint1 = new Complaint("a", 0, 0, LocalDate.now());
        Complaint complaint2 = new Complaint("b", 0, 0, LocalDate.now());
        Complaint complaint3 = new Complaint("c", 0, 0, LocalDate.now().minusDays(2));
        Complaint complaint4 = new Complaint("d", 0, 0, LocalDate.now().minusDays(2));
        Complaint complaint5 = new Complaint("e", 0, 0, LocalDate.now().minusDays(3));
        Complaint complaint6 = new Complaint("f", 0, 0, LocalDate.now().minusDays(4));
        complaint1.setAccepted(true);
        complaint4.setAccepted(true);
        complaint5.setAccepted(true);
        List<Complaint> complaints = new ArrayList<>();
        complaints.add(complaint1);
        complaints.add(complaint1);
        complaints.add(complaint1);
        complaints.add(complaint2);
        complaints.add(complaint3);
        complaints.add(complaint4);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint5);
        complaints.add(complaint5);
        complaints.add(complaint5);
        complaints.add(complaint6);
        AllComplaintsEvent complaintsEvent = new AllComplaintsEvent(complaints);
        if(reportType.equals("income")){
            getOrders(allOrdersEventIncome);
        }
        else if(reportType.equals("orders")){
            getOrders(allOrdersEventOrders);
        }
        else if(reportType.equals("complaints")){
            getComplaints(complaintsEvent);
        }
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    @FXML
    void compare(ActionEvent event) {
        startDate = startDatePicker.getValue();
        endDate = endDatePicker.getValue();
        reportType = typesList.getSelectionModel().getSelectedItem();
        shop = shopsList.getSelectionModel().getSelectedItem();
        if(startDate == null || endDate == null || reportType == null || shop == null){
            return;
        }

        compare = true;

        Utils.sendReportMessage(reportType);
        Map map  = new HashMap();
        Order order1 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), -1);
        Order order2 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), -1);
        Order order3 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(2), -1);
        Order order4 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(3), -1);
        Order order5 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), -1);
        Order order6 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), -1);
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);
        orders.add(order6);
        AllOrdersEvent allOrdersEventIncome = new AllOrdersEvent(orders);

        Product product1 = new Product(1, "a", "plant", "a", 1, null);
        Product product2 = new Product(2, "b", "plant", "b", 1, null);
        Product product3 = new Product(3, "c", "flower", "c", 1, null);
        Product product4 = new Product(4, "d", "flower", "d", 1, null);
        Product product5 = new Product(5, "e", "flower", "e", 1, null);
        Product product6 = new Product(6, "f", "bouquet", "f", 1, null);
        Product product7 = new Product(7, "g", "bouquet", "g", 1, null);

        Map<BaseProduct, Integer> products1 = new HashMap();
        products1.put(product1, 2);
        products1.put(product3, 3);
        Map<BaseProduct, Integer> products2 = new HashMap();
        products2.put(product3, 3);
        products2.put(product4, 4);
        products2.put(product5, 5);
        Map<BaseProduct, Integer> products3 = new HashMap();
        products3.put(product6, 4);
        products3.put(product7, 6);
        products3.put(product2, 3);
        Map<BaseProduct, Integer> products4 = new HashMap();
        products4.put(product4, 4);

        Order order7 = new Order(products1, "a", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 1);
        Order order8 = new Order(products2, "b", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 2);
        Order order9 = new Order(products3, "c", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 3);
        Order order10 = new Order(products4, "d", "a", "a", "a", LocalDateTime.now(), LocalDate.now(), 4);

        List<Order> orders2 = new ArrayList<>();
        orders.add(order7);
        orders.add(order8);
        orders.add(order9);
        orders.add(order10);
        AllOrdersEvent allOrdersEventOrders = new AllOrdersEvent(orders);

        Complaint complaint1 = new Complaint("a", 0, 0, LocalDate.now());
        Complaint complaint2 = new Complaint("b", 0, 0, LocalDate.now());
        Complaint complaint3 = new Complaint("c", 0, 0, LocalDate.now().minusDays(2));
        Complaint complaint4 = new Complaint("d", 0, 0, LocalDate.now().minusDays(2));
        Complaint complaint5 = new Complaint("e", 0, 0, LocalDate.now().minusDays(3));
        Complaint complaint6 = new Complaint("f", 0, 0, LocalDate.now().minusDays(4));
        complaint1.setAccepted(true);
        complaint4.setAccepted(true);
        complaint5.setAccepted(true);
        List<Complaint> complaints = new ArrayList<>();
        complaints.add(complaint1);
        complaints.add(complaint1);
        complaints.add(complaint1);
        complaints.add(complaint2);
        complaints.add(complaint3);
        complaints.add(complaint4);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint3);
        complaints.add(complaint5);
        complaints.add(complaint5);
        complaints.add(complaint5);
        complaints.add(complaint6);
        AllComplaintsEvent complaintsEvent = new AllComplaintsEvent(complaints);
        if(reportType.equals("income")){
            getOrders(allOrdersEventIncome);
        }
        else if(reportType.equals("orders")){
            getOrders(allOrdersEventOrders);
        }
        else if(reportType.equals("complaints")){
            getComplaints(complaintsEvent);
        }
    }

    @FXML
    void showCompareOptions(ActionEvent event) {
        boolean visible = !startLabel.isVisible();
        Platform.runLater(()->startLabel.setVisible(visible));
        Platform.runLater(()->endLabel.setVisible(visible));
        Platform.runLater(()->startDatePicker.setVisible(visible));
        Platform.runLater(()->endDatePicker.setVisible(visible));
        Platform.runLater(()->typeLabel.setVisible(visible));
        Platform.runLater(()->typesList.setVisible(visible));
        Platform.runLater(()->shopLabel.setVisible(visible));
        Platform.runLater(()->shopsList.setVisible(visible));
        Platform.runLater(()->compareButton.setVisible(visible));


    }

    private void callReportOrderList(List<Order> orders, BarChart<String, Number> histogram, String type, LocalDate startDate, LocalDate endDate, String shop){
        if(type.equals("income")){
            Utils.incomeReport(orders, histogram, startDate, endDate, shop);
        }
        else if(type.equals("orders")){
            Utils.ordersReport(orders, histogram, startDate, endDate, shop);
        }
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

}
