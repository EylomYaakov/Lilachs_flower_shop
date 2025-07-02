package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ItemController {

    @FXML
    private Button catalogBtn;

    @FXML
    private TextArea description;

    @FXML
    private TextField price;

    @FXML
    private Label statusLabel;

    @FXML
    private Button updatePriceBtn;

    @FXML
    private Button loginButton;

    int id;



    public void initialize() {
        EventBus.getDefault().register(this);
        try {
            id = SimpleClient.getClient().getLastItemId();
            SimpleClient.getClient().sendToServer("GET_ITEM:" + id);
            if(!SimpleClient.getClient().getAccountType().equals("worker")){
                description.setEditable(false);
                price.setEditable(false);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void backToCatalog(ActionEvent event) {
        try {
            App.setRoot("catalog");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void updatePrice(ActionEvent event) {
        String priceString = price.getText();
        double newPrice = 0;
        try {
            newPrice = Double.parseDouble(priceString);
            if(newPrice < 0) {
                statusLabel.setText("Invalid price");
            }
            else{
                statusLabel.setText("Price updated");
                try {
                    SimpleClient.getClient().sendToServer("UPDATE_PRICE:" + id + ":"+ newPrice);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid price");
        }
    }

    @FXML
    void loginPressed(ActionEvent event) {
        try {
            App.setRoot("login");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void initDescription(InitDescriptionEvent event){
        Product product = event.getProduct();
        Platform.runLater(() -> description.setText(product.description));
        Platform.runLater(()->price.setText(String.valueOf(product.price)));

    }

}

