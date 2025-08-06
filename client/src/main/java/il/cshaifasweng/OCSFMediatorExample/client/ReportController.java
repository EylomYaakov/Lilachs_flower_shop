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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;

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
        if(orders.isEmpty()){
            System.out.println("No orders found");
        }
        for(Order order : orders){
            System.out.println("order shop:" + order.getShop());
        }
        List<Order> filteredOrders = filterOrders(orders, startDate, endDate, shop);
        List<LocalDate> filteredDates = filterDates(subscriptionDates, startDate, endDate);
        if(compare) {
            try {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("compareReports.fxml"));
                Parent root = loader.load();
                CompareReportsController controller = loader.getController();
                controller.setHistogram1(reportHistogram);
                callReportOrderList(filteredOrders, controller.getHistogram2(), reportType, startDate, endDate, shop, filteredDates);
                Platform.runLater(() -> {
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) reportHistogram.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                });
                return;
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        callReportOrderList(filteredOrders, reportHistogram, reportType, startDate, endDate, shop, filteredDates);
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
                Platform.runLater(() -> {
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) reportHistogram.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                });
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
