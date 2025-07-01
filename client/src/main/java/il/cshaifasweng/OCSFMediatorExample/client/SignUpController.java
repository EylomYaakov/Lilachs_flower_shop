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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.io.IOException;

public class SignUpController {

    @FXML
    private MenuButton accountType;

    @FXML
    private TextField creditCard;

    @FXML
    private TextField idField;

    @FXML
    private TextField password;

    @FXML
    private Button signUpButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField username;

    @FXML
    void signUpPressed(ActionEvent event) {
        String signupAttempt = "SIGNUP:" + username.getText() + ":" + password.getText() + ":" + idField.getText() + ":" + creditCard.getText();
        try {
            SimpleClient.getClient().sendToServer(signupAttempt);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void accountTypePressed(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        accountType.setText(item.getText());
    }

}
