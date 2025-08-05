package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SignUpController {

    @FXML
    private ComboBox<String> typesList;

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

    public void initialize() {
        EventBus.getDefault().register(this);
        assert typesList != null : "fx:id=\"listBox\" was not injected: check your FXML file 'primary.fxml'.";
        List<String> shops = CatalogController.getShops();
        Platform.runLater(() -> typesList.getItems().add("chain account"));
        Platform.runLater(() -> typesList.getItems().add("subscription"));
        for (String shop : shops) {
            Platform.runLater(() -> typesList.getItems().add("shop account: " + shop));
        }

    }

    private boolean onlyDigits(String str){
        for(int i=0; i<str.length(); i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }


    private String checkDetails(String username, String password, String id, String creditCard, String type) {
        if(username.isEmpty() || password.isEmpty() || id.isEmpty() || creditCard.isEmpty() || type==null){
            return "please fill all fields";
        }
        String notValid = "";
        if(!Utils.isValidPassword(password)){
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
        String accountType = typesList.getSelectionModel().getSelectedItem();
        String valid  = checkDetails(username.getText(), password.getText(), idField.getText(), creditCard.getText(), accountType);
        if(!valid.isEmpty()){
            Platform.runLater(()-> statusLabel.setText(valid));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
        else {
            if(accountType.startsWith("shop")){
                accountType = accountType.substring(14);
            }
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String formattedDate = today.format(formatter);
            ConnectedUser newUser = new ConnectedUser(-1,username.getText(), password.getText(), idField.getText(), creditCard.getText(), accountType,formattedDate);
            SimpleClient.setUser(newUser);
            Platform.runLater(()-> statusLabel.setText("valid"));
            try {
                SimpleClient.getClient().sendToServer(newUser);
                App.switchScreen("menu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void loginPressed(ActionEvent event) {
        App.switchScreen("login");
    }

    @Subscribe
    public void signUpAttempt(SignUpEvent event){
        String status = event.getStatus();
        if(status.startsWith("SIGN_SUCCESS")){
            try {
                SimpleClient.setUser(event.getConnectedUser());
                SimpleClient.setId(event.getId());
                SimpleClient.getClient().setRole(event.getConnectedUser().getRole());
                Platform.runLater(()-> statusLabel.setText("sign up successfully"));

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Platform.runLater(()-> statusLabel.setText("username already exists"));
            Platform.runLater(()-> statusLabel.setStyle("-fx-text-fill: red;"));
        }
    }

    @FXML
    void backToCatalog(ActionEvent event) {
        App.switchScreen("Catalog");
    }

}
