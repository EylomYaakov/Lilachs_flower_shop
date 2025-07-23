package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UsersController {

    @FXML
    private Button changePasswordButton1;

    @FXML
    private Button changePasswordButton2;

    @FXML
    private Button changeRoleButton1;

    @FXML
    private Button changeRoleButton2;

    @FXML
    private Button changeUsernameButton1;

    @FXML
    private Button changeUsernameButton2;

    @FXML
    private Button leftArrow;

    @FXML
    private TextField password1;

    @FXML
    private TextField password2;

    @FXML
    private Label passwordLabel1;

    @FXML
    private Label passwordLabel2;

    @FXML
    private Label passwordStatus1;

    @FXML
    private Label passwordStatus2;

    @FXML
    private Button rightArrow;

    @FXML
    private ComboBox<String> role1;

    @FXML
    private ComboBox<String> role2;

    @FXML
    private Label roleLabel1;

    @FXML
    private Label roleLabel2;

    @FXML
    private Label roleStatus1;

    @FXML
    private Label roleStatus2;


    @FXML
    private TextField username1;

    @FXML
    private TextField username2;

    @FXML
    private Label usernameLabel1;

    @FXML
    private Label usernameLabel2;

    @FXML
    private Label usernameStatus1;

    @FXML
    private Label usernameStatus2;



    private Label[][] labels;
    private Button[][] buttons;
    private TextField[] usernames;
    private TextField[] passwords;
    private ComboBox<String>[] roles;
    private Label[] usernameStatusLabels;
    private Label[] passwordStatusLabels;
    private Label[] roleStatusLabels;

    private List<ConnectedUser> users;
    private Paginator<ConnectedUser> paginator;
    private final int pageSize = 2;
    private boolean[] changeUsername;

    @FXML
    public void initialize() {
        labels = new Label[][] {{usernameLabel1, passwordLabel1, roleLabel1}, {usernameLabel2, passwordLabel2, roleLabel2}};
        buttons = new Button[][] {{changeUsernameButton1, changePasswordButton1, changeRoleButton1}, {changeUsernameButton2, changePasswordButton2, changeRoleButton2}};
        usernames = new TextField[] {username1, username2};
        passwords = new TextField[] {password1, password2};
        roles = new ComboBox[] {role1, role2};
        usernameStatusLabels = new Label[] {usernameStatus1, usernameStatus2};
        passwordStatusLabels = new Label[] {passwordStatus1, passwordStatus2};
        roleStatusLabels = new Label[] {roleStatus1, roleStatus2};
        changeUsername = new boolean[usernames.length];
        for(ComboBox<String> role : roles) {
            role.getItems().addAll("customer", "worker");
        }
        try{
            SimpleClient.getClient().sendToServer("GET_USERS");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //only for users to be not empty
        users = new ArrayList<>();
        ConnectedUser user1 = new ConnectedUser("Eylom", "12345678", "1", "1", "worker", "all chain");
        ConnectedUser user2 = new ConnectedUser("Ofek", "Aa1234", "2", "2", "customer", "all chain");
        ConnectedUser user3 = new ConnectedUser("Ariel", "a123451", "3", "2", "worker", "all chain");
        ConnectedUser user4 = new ConnectedUser("Hodaya", "1234567", "4", "2", "customer", "all chain");
        ConnectedUser user5 = new ConnectedUser("Mike", "123456", "5", "2", "worker", "all chain");
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        users.add(user5);
        paginator = new Paginator<>(users, pageSize);
        renderPage();
    }


    @Subscribe
    public void initUsers(UsersListEvent event) {
        List<ConnectedUser> users = event.getUsers();
        this.users = users;
        paginator = new Paginator<>(users, pageSize);
        renderPage();
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

    @FXML
    void changePassword(ActionEvent event) {
        Button source = (Button) event.getSource();
        int passwordIndex = 1;
        for(int i=0; i<passwords.length; i++) {
            if(buttons[i][passwordIndex] == source) {
                int finalI = i;
                String password = passwords[i].getText();
                ConnectedUser user = users.get(paginator.getCurrentIndex()-paginator.getCurrentPageSize()+i);
                if(password.equals(user.getPassword())) {
                    return;
                }
                if(!Utils.isValidPassword(password)) {
                    Platform.runLater(()->passwordStatusLabels[finalI].setText("password does not meet requirements"));
                    Platform.runLater(()->passwordStatusLabels[finalI].setStyle("-fx-font: 10 System; -fx-text-fill: red;"));
                    return;
                }
                user.setPassword(password);
                Platform.runLater(()->passwordStatusLabels[finalI].setText("password changed!"));
                Platform.runLater(()->passwordStatusLabels[finalI].setStyle(""));
                try{
                    SimpleClient.getClient().sendToServer(user);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @FXML
    void changeRole(ActionEvent event) {
        Button source = (Button) event.getSource();
        int roleIndex = 2;
        for(int i=0; i<roles.length; i++) {
            if(buttons[i][roleIndex] == source) {
                int finalI = i;
                ConnectedUser user = users.get(paginator.getCurrentIndex()-paginator.getCurrentPageSize()+i);
                if(roles[i].getSelectionModel().getSelectedItem().equals(user.getRole())) {
                    return;
                }
                user.setRole(roles[i].getSelectionModel().getSelectedItem());
                Platform.runLater(()->roleStatusLabels[finalI].setText("role changed!"));
                try{
                    SimpleClient.getClient().sendToServer(user);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @FXML
    void changeUsername(ActionEvent event) {
        Button source = (Button) event.getSource();
        int usernameIndex = 0;
        for(int i=0; i<roles.length; i++) {
            if(buttons[i][usernameIndex] == source) {
                int finalI = i;
                ConnectedUser user = users.get(paginator.getCurrentIndex()-paginator.getCurrentPageSize()+i);
                if(usernames[i].getText().equals(user.getUsername())){
                    return;
                }
                user.setUsername(usernames[i].getText());
                changeUsername[i] = true;
                try{
                    SimpleClient.getClient().sendToServer(user);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @Subscribe
    public void changeUsernameAttempt(ChangeUserDetailsEvent event) {
        for(int i=0; i<changeUsername.length; i++) {
            if(changeUsername[i]) {
                int finalI = i;
                if(event.getStatus().equals("SUCCESS")) {
                    Platform.runLater(() -> usernameStatusLabels[finalI].setText("username changed!"));
                    Platform.runLater(()-> usernameStatusLabels[finalI].setStyle(""));
                }
                else{
                    Platform.runLater(() -> usernameStatusLabels[finalI].setText("username already exists!"));
                    Platform.runLater(()-> usernameStatusLabels[finalI].setStyle("-fx-text-fill: red;"));
                }
            }
        }
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        paginator.prevPage();
        renderPage();
    }

    @FXML
    void rightArrowPressed(ActionEvent event) {
        renderPage();
    }


    public void renderPage() {
        List<ConnectedUser> pageItems = paginator.getCurrentPageItems();
        for(int i=0; i<usernames.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setUserVisibility(i,true);
                renderUser(i);
            }
            else{
                setUserVisibility(i,false);
            }
        }
        Platform.runLater(()->rightArrow.setVisible(paginator.hasNextPage()));
        Platform.runLater(()->leftArrow.setVisible(paginator.hasPreviousPage()));
    }

    private void setUserVisibility(int i, boolean visible){
        for(Label label : labels[i]){
            label.setVisible(visible);
        }
        for(Button button : buttons[i]){
            button.setVisible(visible);
        }
        Platform.runLater(()-> usernameStatusLabels[i].setVisible(visible));
        Platform.runLater(()-> passwordStatusLabels[i].setVisible(visible));
        Platform.runLater(()-> usernames[i].setVisible(visible));
        Platform.runLater(()-> passwords[i].setVisible(visible));
        Platform.runLater(()-> roles[i].setVisible(visible));
        Platform.runLater(()-> roleStatusLabels[i].setText(""));
        Platform.runLater(()-> usernameStatusLabels[i].setText(""));
        Platform.runLater(()-> passwordStatusLabels[i].setText(""));
    }

    private void renderUser(int index){
        ConnectedUser user = users.get(paginator.getCurrentIndex()-paginator.getCurrentPageSize()+index);
        Platform.runLater(()->usernames[index].setText(user.getUsername()));
        Platform.runLater(()->passwords[index].setText(user.getPassword()));
        Platform.runLater(()->roles[index].getSelectionModel().select(user.getRole()));
    }

}
