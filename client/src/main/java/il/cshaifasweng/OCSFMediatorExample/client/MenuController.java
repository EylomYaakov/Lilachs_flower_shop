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
    private Button workerComplaintsButton;

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
            Platform.runLater(()->addItemButton.setVisible(false));
            if(SimpleClient.getRole().equals("subscription")){
                Platform.runLater(()->subscriptionButton.setVisible(false));
            }
        }
        else if(role.equals("worker:customer service") || role.startsWith("worker:manager")){
            Platform.runLater(()->workerComplaintsButton.setVisible(true));
        }
        if(role.startsWith("worker:manager")){
            Platform.runLater(()->createReportButton.setVisible(true));
            Platform.runLater(()->usersButton.setVisible(true));
        }

    }

    @FXML
    void buySubscription(ActionEvent event) {
        ConnectedUser user = SimpleClient.getUser();
        user.setRole("subscription");
        Platform.runLater(()->subscriptionStatus.setVisible(true));
        Platform.runLater(()->subscriptionStatus.setStyle("-fx-text-fill: green;"));
        Platform.runLater(()->subscriptionButton.setVisible(false));
        try{
            SimpleClient.getClient().sendToServer("SUBSCRIBED:" + SimpleClient.getId());
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
}
