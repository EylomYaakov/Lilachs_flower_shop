package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ConnectedUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MenuController {

    @FXML
    private Button addItemButton;

    @FXML
    private Button cartButton;

    @FXML
    private Button catlogButton;

    @FXML
    private Button userComplaintsButton;

    @FXML
    private Button WorkerComplaintsButton;

    @FXML
    private Button createReportButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button subscriptionButton;

    @FXML
    private Label subscriptionStatus;

    @FXML
    private Button usersButton;


    @FXML
    public void initialize() {
        String role = SimpleClient.getRole();
        if(!role.startsWith("worker")){
            setUserVisibility(true);
            setWorkerVisibility(false);
            if(SimpleClient.getUser().getAccountType().equals("subscription")){
                Platform.runLater(()->subscriptionButton.setVisible(false));
            }
        }
        else if(!role.startsWith("worker:manager")){
            Platform.runLater(()->usersButton.setVisible(false));
        }

    }

    @FXML
    void buySubscription(ActionEvent event) {
        ConnectedUser user = SimpleClient.getUser();
        user.setAccountType("subscription");
        Platform.runLater(()->subscriptionStatus.setVisible(true));
        Platform.runLater(()->subscriptionStatus.setStyle("-fx-text-fill: green;"));
        try{
            SimpleClient.getClient().sendToServer("SUBSCRIBED:" + SimpleClient.getUser().getUsername());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void toAddItem(ActionEvent event) {
        App.switchScreen("addItem");
    }

    @FXML
    void toCart(ActionEvent event) {
        App.switchScreen("cart");
    }

    @FXML
    void toCatalog(ActionEvent event) {
        App.switchScreen("catalog");
    }

    @FXML
    void toComplaints(ActionEvent event) {
        App.switchScreen("complaints");
    }

    @FXML
    void toCreateReport(ActionEvent event) {
        App.switchScreen("createReport");
    }

    @FXML
    void toOrders(ActionEvent event) {
        App.switchScreen("orders");
    }

    @FXML
    void toUsers(ActionEvent event) {
        App.switchScreen("users");
    }

    private void setUserVisibility(boolean visible){
        Platform.runLater(()->cartButton.setVisible(visible));
        Platform.runLater(()->userComplaintsButton.setVisible(visible));
        Platform.runLater(()->ordersButton.setVisible(visible));
        Platform.runLater(()->subscriptionButton.setVisible(visible));
    }

    private void setWorkerVisibility(boolean visible){
        Platform.runLater(()->addItemButton.setVisible(visible));
        Platform.runLater(()->WorkerComplaintsButton.setVisible(visible));
        Platform.runLater(()->createReportButton.setVisible(visible));
        Platform.runLater(()->usersButton.setVisible(visible));
    }

}
