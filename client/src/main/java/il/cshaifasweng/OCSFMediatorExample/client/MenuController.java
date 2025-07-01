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

public class MenuController {

    @FXML
    private Button CancelOrderButton;

    @FXML
    private Button catlogButton;

    @FXML
    private Button complainButton;

    @FXML
    private Button orderButton;

    @FXML
    void cancelOrder(ActionEvent event) {

    }

    @FXML
    void catalog(ActionEvent event) {
        try {
            App.setRoot("catalog");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void complain(ActionEvent event) {

    }

    @FXML
    void order(ActionEvent event) {

    }

}

