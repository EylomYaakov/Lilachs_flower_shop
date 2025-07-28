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
import org.greenrobot.eventbus.EventBus;
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
    private List<LocalDate> subscriptionDates = new ArrayList<>();

    @FXML
    public void initialize() {
        Utils.initReportOptions(startDatePicker, endDatePicker, typesList, shopsList, shopLabel);
        EventBus.getDefault().register(this);
    }


    @Subscribe
    public void getSubscriptionDates(SubscriptionDatesListEvent event){
        List<LocalDateTime> dates = event.getSubscriptionDates();
        for(LocalDateTime dateWithTime : dates){
            this.subscriptionDates.add(dateWithTime.toLocalDate());
        }
    }

    @Subscribe
    public void getOrders(AllOrdersEvent event) {
        List<Order> orders = event.getOrders();
        List<Order> filterdOrders = filterOrders(orders, startDate, endDate, shop);
        List<LocalDate> filteredDates = filterDates(subscriptionDates, startDate, endDate);
        if(compare) {
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("compareReports.fxml"));
                Parent root = loader.load();
                CompareReportsController controller = loader.getController();
                controller.setHistogram1(reportHistogram);
                callReportOrderList(filterdOrders, controller.getHistogram2(), reportType, startDate, endDate, shop, filteredDates);
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
        callReportOrderList(filterdOrders, reportHistogram, reportType, startDate, endDate, shop, filteredDates);
    }


    @Subscribe
    public void getComplaints(AllComplaintsEvent event) {
        List<Complaint> complaints = event.getComplaints();
        List<Complaint> filteredComplaints = filterComplaints(complaints, startDate, endDate, shop);
        if(compare){
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("compareReports.fxml"));
                Parent root = loader.load();
                CompareReportsController controller = loader.getController();
                controller.setHistogram1(reportHistogram);
                Utils.complaintsReport(filteredComplaints, controller.getHistogram2(), startDate, endDate, shop);
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
       Utils.complaintsReport(filteredComplaints, reportHistogram, startDate, endDate, shop);
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;

        Map map  = new HashMap();
        Order order1 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), 200,-1);
        Order order2 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), 100,-1);
        Order order3 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(2), 300,-1);
        Order order4 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(3), 20,-1);
        Order order5 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5),55, -1);
        Order order6 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), 70, -1);
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);
        orders.add(order6);

        Product product1 = new Product(1, "a", "plant", "a", 1, null, "Haifa");
        Product product2 = new Product(2, "b", "plant", "b", 1, null, "Haifa");
        Product product3 = new Product(3, "c", "flower", "c", 1, null, "Tel Aviv");
        Product product4 = new Product(4, "d", "flower", "d", 1, null, "Tel Aviv");
        Product product5 = new Product(5, "e", "flower", "e", 1, null, "all chain");
        Product product6 = new Product(6, "f", "bouquet", "f", 1, null, "all chain");
        Product product7 = new Product(7, "g", "bouquet", "g", 1, null, "Jerusalem");

        Map<BaseProduct, Integer> products1 = new HashMap();
        products1.put(product1, 2);
        products1.put(product2, 3);
        Map<BaseProduct, Integer> products2 = new HashMap();
        products2.put(product3, 3);
        products2.put(product4, 4);
        products2.put(product5, 5);
        Map<BaseProduct, Integer> products3 = new HashMap();
        products3.put(product6, 4);
        products3.put(product7, 6);
        products3.put(product5, 3);
        Map<BaseProduct, Integer> products4 = new HashMap();
        products4.put(product4, 4);

        Order order7 = new Order(products1, "a", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(2), 80, 1);
        Order order8 = new Order(products2, "b", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(1), 500, 2);
        Order order9 = new Order(products3, "c", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(3), 60, 3);
        Order order10 = new Order(products4, "d", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(2), 100,  4);
        List<Order> orders2 = new ArrayList<>();
        orders2.add(order7);
        orders2.add(order8);
        orders2.add(order9);
        orders2.add(order10);
        AllOrdersEvent allOrdersEventOrders = new AllOrdersEvent(orders2);
        AllOrdersEvent allOrdersEventIncome = new AllOrdersEvent(orders2);

        Complaint complaint1 = new Complaint("a", "Tel Aviv",0, 0, LocalDate.now());
        Complaint complaint2 = new Complaint("b", "Tel aviv",0, 0, LocalDate.now());
        Complaint complaint3 = new Complaint("c", "Haifa",0, 0, LocalDate.now().minusDays(2));
        Complaint complaint4 = new Complaint("d", "Haifa",0, 0, LocalDate.now().minusDays(2));
        Complaint complaint5 = new Complaint("e", "Jerusalem",0, 0, LocalDate.now().minusDays(3));
        Complaint complaint6 = new Complaint("f", "Jerusalem",0, 0, LocalDate.now().minusDays(4));
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

        Map map  = new HashMap();
        Order order1 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), 200,-1);
        Order order2 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now(), 100,-1);
        Order order3 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(2), 300,-1);
        Order order4 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(3), 20,-1);
        Order order5 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5),55, -1);
        Order order6 = new Order(map, "a", "a", "a", "a",  LocalDateTime.now(), LocalDate.now().minusDays(5), 70, -1);
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);
        orders.add(order6);

        Product product1 = new Product(1, "a", "plant", "a", 1, null, "Haifa");
        Product product2 = new Product(2, "b", "plant", "b", 1, null, "Haifa");
        Product product3 = new Product(3, "c", "flower", "c", 1, null, "Tel Aviv");
        Product product4 = new Product(4, "d", "flower", "d", 1, null, "Tel Aviv");
        Product product5 = new Product(5, "e", "flower", "e", 1, null, "all chain");
        Product product6 = new Product(6, "f", "bouquet", "f", 1, null, "all chain");
        Product product7 = new Product(7, "g", "bouquet", "g", 1, null, "Jerusalem");

        Map<BaseProduct, Integer> products1 = new HashMap();
        products1.put(product1, 2);
        products1.put(product2, 3);
        Map<BaseProduct, Integer> products2 = new HashMap();
        products2.put(product3, 3);
        products2.put(product4, 4);
        products2.put(product5, 5);
        Map<BaseProduct, Integer> products3 = new HashMap();
        products3.put(product6, 4);
        products3.put(product7, 6);
        products3.put(product5, 3);
        Map<BaseProduct, Integer> products4 = new HashMap();
        products4.put(product4, 4);

        Order order7 = new Order(products1, "a", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(2), 80, 1);
        Order order8 = new Order(products2, "b", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(1), 500, 2);
        Order order9 = new Order(products3, "c", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(3), 60, 3);
        Order order10 = new Order(products4, "d", "a", "a", "a", LocalDateTime.now(), LocalDate.now().minusDays(2), 100,  4);

        List<Order> orders2 = new ArrayList<>();
        orders2.add(order7);
        orders2.add(order8);
        orders2.add(order9);
        orders2.add(order10);
        AllOrdersEvent allOrdersEventOrders = new AllOrdersEvent(orders2);
        AllOrdersEvent allOrdersEventIncome = new AllOrdersEvent(orders2);

        Complaint complaint1 = new Complaint("a", "Tel Aviv",0, 0, LocalDate.now());
        Complaint complaint2 = new Complaint("b", "Tel aviv",0, 0, LocalDate.now());
        Complaint complaint3 = new Complaint("c", "Haifa",0, 0, LocalDate.now().minusDays(2));
        Complaint complaint4 = new Complaint("d", "Haifa",0, 0, LocalDate.now().minusDays(2));
        Complaint complaint5 = new Complaint("e", "Jerusalem",0, 0, LocalDate.now().minusDays(3));
        Complaint complaint6 = new Complaint("f", "Jerusalem",0, 0, LocalDate.now().minusDays(4));
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
            System.out.println("Income Report");
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
        Platform.runLater(()->compareButton.setVisible(visible));
        if(SimpleClient.getUser().getShop().equals("all chain")) {
            Platform.runLater(() -> shopLabel.setVisible(visible));
            Platform.runLater(() -> shopsList.setVisible(visible));
        }


    }

    private void callReportOrderList(List<Order> orders, BarChart<String, Number> histogram, String type, LocalDate startDate, LocalDate endDate, String shop, List<LocalDate> dates){
        if(type.equals("income")){
            Utils.incomeReport(orders, histogram, startDate, endDate, shop, dates);
        }
        else if(type.equals("orders")){
            Utils.ordersReport(orders, histogram, startDate, endDate, shop);
        }
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }


    private List<Order> filterOrders(List<Order> orders, LocalDate startDate, LocalDate endDate, String shop){
        List<Order> filteredOrders = new ArrayList<>();
        for(Order order : orders){
            LocalDate orderDate = order.getOrderDate();
            if(orderDate.isAfter(startDate.minusDays(1)) && orderDate.isBefore(endDate.plusDays(1))) {
                if(order.getShop().equals(shop) || shop.equals("all chain")) {
                    filteredOrders.add(order);
                }
            }
        }
        return filteredOrders;
    }

    private List<Complaint> filterComplaints(List<Complaint> complaints, LocalDate startDate, LocalDate endDate, String shop){
        List<Complaint> filteredComplaints = new ArrayList<>();
        for(Complaint complaint : complaints){
            LocalDate complaintDate = complaint.getDate();
            if(complaintDate.isAfter(startDate.minusDays(1)) && complaintDate.isBefore(endDate.plusDays(1))) {
               if(complaint.getShop().equals(shop) || shop.equals("all chain")) {
                   filteredComplaints.add(complaint);
               }
            }
        }
        return filteredComplaints;
    }

    private List<LocalDate> filterDates(List<LocalDate> dates, LocalDate startDate, LocalDate endDate){
        List<LocalDate> filteredDates = new ArrayList<>();
        for(LocalDate date : dates){
            if(date.isBefore(endDate.plusDays(1)) && date.isAfter(startDate.minusDays(1))) {
                filteredDates.add(date);
            }
        }
        return filteredDates;
    }

}
