package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.BaseProduct;
import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.OrdersListEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersController {

    @FXML
    private Button button1;

    @FXML
    private Button button2;

    @FXML
    private Button button3;

    @FXML
    private TextField complaint1;

    @FXML
    private TextField complaint2;

    @FXML
    private TextField complaint3;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;

    @FXML
    private Label ordersEmpty;

    @FXML
    private Button leftArrow;

    @FXML
    private Button rightArrow;

    @FXML
    private Button sendComplaintButton2;

    @FXML
    private Button sendComplaintButton1;

    @FXML
    private Button sendComplaintButton3;

    @FXML
    private Label statusLabel;

    private Button[] buttons;
    private Label[] orderLabels;
    private TextField[] complaints;
    private Button[] sendComplaintButtons;
    private final int pageSize = 3;
    private List<Order> orders;
    private Paginator<Order> paginator;

    @FXML
    public void initialize() {
        buttons = new Button[]{button1, button2, button3};
        orderLabels = new Label[]{label1, label2, label3};
        complaints = new TextField[]{complaint1, complaint2, complaint3};
        sendComplaintButtons = new Button[]{sendComplaintButton1, sendComplaintButton2, sendComplaintButton3};
        EventBus.getDefault().register(this);
        try{
            SimpleClient.getClient().sendToServer("GET_ORDERS:" + SimpleClient.getId());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //only for orders to be not empty
        orders = new ArrayList<>();
        Map<BaseProduct, Integer> map = new HashMap<>();
        Order order1 = new Order(map, "", "", "", "",  LocalDateTime.now().plusHours(2), LocalDate.now(),10, -1);
        Order order2 = new Order(map, "", "", "", "",  LocalDateTime.now().plusHours(4),LocalDate.now(), 10,-1);
        Order order3 = new Order(map, "", "", "", "", LocalDateTime.now().plusMinutes(30),LocalDate.now(),10,-1);
        Order order4 = new Order(map, "", "", "", "", LocalDateTime.now().minusHours(2),LocalDate.now(),10,-1);
        Order order5 = new Order(map, "", "", "", "",  LocalDateTime.now().minusHours(4),LocalDate.now(),10,-1);
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orders.add(order4);
        orders.add(order5);
        OrdersListEvent ordersListEvent = new OrdersListEvent(orders);
        initOrders(ordersListEvent);
    }

    @Subscribe
    public void initOrders(OrdersListEvent event){
        orders = event.getOrders();
        paginator = new Paginator<>(this.orders, pageSize);
        renderPage();
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        paginator.prevPage();
        renderPage();
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        Button button = (Button) event.getSource();
        for(int i = 0; i < buttons.length; i++){
            if(button == buttons[i]){
                if(button.getText().equals("complain")){
                    setComplaintVisibility(i, !complaints[i].isVisible());
                    return;
                }
                else if(button.getText().equals("cancel order")) {
                    cancelOrder(i);
                    return;
                }
            }
        }
    }

    @FXML
    void rightArrowPressed(ActionEvent event) {
        renderPage();
    }

    @FXML
    void sendComplaint(ActionEvent event) {
        Button source = (Button) event.getSource();
        for(int i = 0; i < sendComplaintButtons.length; i++){
            if(source == sendComplaintButtons[i]){
                String customerComplaint = complaints[i].getText();
                Order order  = paginator.getItem(i);
                order.setComplained(true);
                if(!customerComplaint.isEmpty()){
                    try{
                        Complaint complaint = new Complaint(customerComplaint, order.getShop(), order.getId(), SimpleClient.getId(), LocalDate.now());
                        SimpleClient.getClient().sendToServer(complaint);
                        Platform.runLater(() -> statusLabel.setText("complaint sent"));
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void renderPage(){
        List<Order> pageItems = paginator.getCurrentPageItems();
        if(pageItems.isEmpty()){
            Platform.runLater(()->ordersEmpty.setVisible(true));
        }
        else{
            Platform.runLater(()->ordersEmpty.setVisible(false));
        }
        for(int i=0; i<orderLabels.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setOrderVisibilty(i,true);
                setComplaintVisibility(i, false);
                Order order = pageItems.get(i);
                LocalDateTime date = order.getDeliveryTime();
                String text = "";
                Platform.runLater(()->buttons[finalI].setVisible(true));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                String formattedTime = date.format(formatter);
                if(order.isCancelled()){
                    text = "order scheduled to " + formattedTime + " - cancelled!";
                    Platform.runLater(()->buttons[finalI].setVisible(false));
                }
                else if(date.isAfter(LocalDateTime.now())){
                    text = "Order scheduled to " + formattedTime;
                    Platform.runLater(()->buttons[finalI].setText("cancel order"));
                }
                else{
                    text = "Order delivered at " + formattedTime;
                    if(order.isComplained()){
                        Platform.runLater(()->buttons[finalI].setText("complaint sent"));
                        Platform.runLater(()->buttons[finalI].setDisable(true));
                    }
                    else {
                        Platform.runLater(() -> buttons[finalI].setText("complain"));
                    }
                }
                String finalText = text;
                Platform.runLater(() -> orderLabels[finalI].setText(finalText));
            }
            else{
                setOrderVisibilty(i,false);
            }
        }
        Platform.runLater(()->rightArrow.setVisible(paginator.hasNextPage()));
        Platform.runLater(()->leftArrow.setVisible(paginator.hasPreviousPage()));
    }

    public void setOrderVisibilty(int i, boolean visible){
        Platform.runLater(() -> orderLabels[i].setVisible(visible));
        Platform.runLater(()-> buttons[i].setVisible(visible));
        Platform.runLater(()->buttons[i].setDisable(false));
    }

    public String getOrderDescription(Order order){
        LocalDateTime date = order.getDeliveryTime();
        if(date.isAfter(LocalDateTime.now())){
            return "Order will be deliver at " + date.toString();
        }
        else{
            return "Order delivered at " + date.toString();
        }

    }

    private void setComplaintVisibility(int i, boolean visible){
        Platform.runLater(() -> complaints[i].setVisible(visible));
        Platform.runLater(() -> sendComplaintButtons[i].setVisible(visible));
    }

    private void cancelOrder(int index){
        Order order = paginator.getItem(index);
        order.setCancelled(true);
        int id = order.getId();
        try{
            SimpleClient.getClient().sendToServer("CANCEL_ORDER:" + id);
            paginator.setCurrentIndex(paginator.getCurrentIndex()-paginator.getCurrentPageSize());
            renderPage();
            LocalDateTime delivery = order.getDeliveryTime();
            long remainingHours = Duration.between(LocalDateTime.now(), delivery).toHours();
            if(remainingHours >= 3){
                order.setRefund(1);
                Platform.runLater(()->statusLabel.setText("Order cancelled - you will get 100% refund"));
            }
            else if(remainingHours >= 1){
                order.setRefund(0.5);
                Platform.runLater(()->statusLabel.setText("Order cancelled - you will get 50% refund"));
            }
            else{
                Platform.runLater(()->statusLabel.setText("Order cancelled"));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


}
