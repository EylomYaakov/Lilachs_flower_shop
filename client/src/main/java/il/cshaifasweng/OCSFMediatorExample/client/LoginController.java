package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.LoginEvent;
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

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private TextField password;

    @FXML
    private Button signUpButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField username;

    public void initialize() {
        statusLabel.setText("");
        EventBus.getDefault().register(this);
    }

    @FXML
    void loginPressed(ActionEvent event) {
        String loginAttempt = "LOGIN:" + username.getText() + ":" + password.getText();
        try {
            SimpleClient.getClient().sendToServer(loginAttempt);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void signUpPressed(ActionEvent event) {
        try {
            App.setRoot("signup");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void loginAttempt(LoginEvent event) {
        Platform.runLater(() -> {
            if (event.getStatus().equals("LOGIN_SUCCESS")) {
                username.setText("");
                password.setText("");
                statusLabel.setText("success");
            } else if (event.getStatus().equals("Already logged in")){
                statusLabel.setText("Already logged in");
            }
            else if (event.getStatus().equals("LOGIN_FAIL")){
                statusLabel.setText("invalid username or password");
            }
        });
    }



}
