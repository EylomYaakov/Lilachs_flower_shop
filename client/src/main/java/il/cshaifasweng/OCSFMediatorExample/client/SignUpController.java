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
    private Label statusLabel;


    @FXML
    private TextField username;

    private boolean onlyDigits(String str){
        for(int i=0; i<str.length(); i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    private boolean isValidPassword(String password){
        boolean captial = false, lowercase = false, number = false;
        for(int i=0; i<password.length(); i++){
            if(Character.isUpperCase(password.charAt(i))){
                captial = true;
            }
            else if(Character.isLowerCase(password.charAt(i))){
                lowercase = true;
            }
            else if(Character.isDigit(password.charAt(i))){
                number = true;
            }
        }
        return captial && lowercase && number && password.length() >= 8;
    }

    private String checkDetails(String username, String password, String id, String creditCard, String type) {
        if(username.isEmpty() || password.isEmpty() || id.isEmpty() || creditCard.isEmpty() || type.equals("account type")){
            return "please fill all fields";
        }
        String notValid = "";
        if(!isValidPassword(password)){
            notValid += "\npassword does not meet requirements";
        }
        if(creditCard.length() != 16 || !onlyDigits(creditCard)){
            notValid += "\ninvalid credit card number";
        }
        if(id.length() != 9 || !onlyDigits(id)){
            notValid += "\ninvalid id";
        }
        if(notValid.isEmpty()){
            return notValid;
        }
        return notValid.substring(1);
    }

    @FXML
    void signUpPressed(ActionEvent event) {
        String valid  = checkDetails(username.getText(), password.getText(), idField.getText(), creditCard.getText(), accountType.getText());
        if(!valid.isEmpty()){
            Platform.runLater(()-> statusLabel.setText(valid));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
        else {
            String signupAttempt = "SIGNUP:" + username.getText() + ":" + password.getText() + ":" + idField.getText() + ":" + creditCard.getText() + ":" + accountType.getText();
            try {
                SimpleClient.getClient().sendToServer(signupAttempt);
                SimpleClient.getClient().setLoggedIn(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void accountTypePressed(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        Platform.runLater(()-> accountType.setText(item.getText()));
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

    @FXML
    void backToCatalog(ActionEvent event) {
        try {
            App.setRoot("catalog");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
