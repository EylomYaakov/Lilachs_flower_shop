package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.LoginEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
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
        Platform.runLater(()->statusLabel.setText(""));
        EventBus.getDefault().register(this);
    }


    @FXML
    void loginPressed(ActionEvent event) {
        if(username.getText().isEmpty() || password.getText().isEmpty()){
            Platform.runLater(()->statusLabel.setText("please fill all fields"));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
        else {
            String loginAttempt = "LOGIN:" + username.getText() + ":" + password.getText();
            try {
                SimpleClient.getClient().sendToServer(loginAttempt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void signUpPressed(ActionEvent event) {
        App.switchScreen("signup");
    }

    @Subscribe
    public void loginAttempt(LoginEvent event){
        String status = event.getStatus();
        if(status.startsWith("LOGIN_SUCCESS")){
            Platform.runLater(()->username.setText(""));
            Platform.runLater(()->password.setText(""));
            App.switchScreen("menu");
        }
        else if(status.startsWith("Already logged in")){
            Platform.runLater(()->statusLabel.setText("Already logged in"));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
        else{
            Platform.runLater(()->statusLabel.setText("invalid username or password"));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
    }


    @FXML
    void backToCatalog(ActionEvent event) {
        App.switchScreen("catalog");
    }

}
