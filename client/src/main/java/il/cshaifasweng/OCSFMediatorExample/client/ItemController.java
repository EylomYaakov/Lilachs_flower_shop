package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ItemController {



    @FXML
    private TextField description;

    @FXML
    private Label imageLabel;


    @FXML
    private ImageView image;

    @FXML
    private TextField imagePath;

    @FXML
    private Button loginButton;

    @FXML
    private TextField name;

    @FXML
    private TextField price;

    @FXML
    private Label statusLabel;

    @FXML
    private Label addedToCartLabel;

    @FXML
    private TextField type;

    @FXML
    private Button updateButton;

    @FXML
    private Button removeItemButton;

    private Product currentItem;

    @FXML
    private Spinner<Integer> amountSpinner;

    int id;


    public void initialize() {
        EventBus.getDefault().register(this);
        try {
            id = SimpleClient.getClient().getLastItemId();
            SimpleClient.getClient().sendToServer("GET_ITEM:" + id);
            amountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000));
            if(!SimpleClient.getClient().getAccountType().equals("worker")){
                Platform.runLater(()->name.setEditable(false));
                Platform.runLater(()->description.setEditable(false));
                Platform.runLater(() -> price.setEditable(false));
                Platform.runLater(() -> type.setEditable(false));
                Platform.runLater(()->updateButton.setVisible(false));
                Platform.runLater(()->imageLabel.setVisible(false));
                Platform.runLater(()->imagePath.setVisible(false));
                Platform.runLater(()->removeItemButton.setVisible(false));
            }
            if(!SimpleClient.getClient().getAccountType().isEmpty()){
                Platform.runLater(()->loginButton.setText("logout"));
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
                Platform.runLater(() -> statusLabel.setText("Invalid price"));
            }
            else{
                Platform.runLater(() -> statusLabel.setText("Price updated"));
                try {
                    SimpleClient.getClient().sendToServer("UPDATE_PRICE:" + id + ":"+ newPrice);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            Platform.runLater(() -> statusLabel.setText("Invalid price"));
        }
    }

    @FXML
    void updateDescription(ActionEvent event) {
        String newDescription = description.getText();
        if(newDescription.isEmpty()) {
            Platform.runLater(() -> statusLabel.setText("description cannot be empty"));
        }
        else {
            Platform.runLater(() -> statusLabel.setText("Description updated"));
            try {
                SimpleClient.getClient().sendToServer("UPDATE_DESCRIPTION:" + id + ":" + newDescription);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    void updateDetails(ActionEvent event) {
        String productName = name.getText();
        String productDescription = description.getText();
        String productImagePath = imagePath.getText();
        String ProductType = type.getText();
        String ProductPrice = price.getText();
        double productPriceDouble = Utils.getPrice(ProductPrice);
        byte[] imageBytes = null;
        Product product = null;
        if(productName.isEmpty() || productDescription.isEmpty() || ProductPrice.isEmpty() || ProductType.isEmpty()) {
            Platform.runLater(() -> statusLabel.setText("please fill all fields"));
            return;
        }
        if(productPriceDouble < 0){
            Platform.runLater(() -> statusLabel.setText("Invalid price"));
            return;
        }
        if(!productImagePath.isEmpty()) {
            if (!Utils.isValidImagePath(productImagePath)) {
                Platform.runLater(() -> statusLabel.setText("Invalid image path"));
                return;
            }
            try {
                imageBytes = Files.readAllBytes(Paths.get(productImagePath));
                Image imageToShow = new Image(new ByteArrayInputStream(imageBytes));
                Platform.runLater(() -> image.setImage(imageToShow));
            } catch (IOException e) {
                e.printStackTrace();
            }
            product = new Product(id ,productName, ProductType, productDescription, productPriceDouble, imageBytes );
        }
        else{
            product = new Product(id ,productName, ProductType, productDescription, productPriceDouble, currentItem.image);
        }
        try {
            SimpleClient.getClient().sendToServer(product);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(()->statusLabel.setText("details updated"));

    }

    @FXML
    void loginPressed(ActionEvent event) {
        try {
            if(loginButton.getText().equals("log in")){
                App.setRoot("login");
            }
            else{
                Platform.runLater(()->loginButton.setText("log in"));
                SimpleClient.getClient().setAccountType("");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void initDetails(InitDescriptionEvent event){
        Product product = event.getProduct();
        Platform.runLater(() -> name.setText(product.description));
        Platform.runLater(() -> description.setText(product.description));
        Platform.runLater(()-> price.setText(String.valueOf(product.price)));
        Platform.runLater(() -> type.setText(product.type));
        Image imageBytes = new Image(new ByteArrayInputStream(product.image));
        Platform.runLater(()->image.setImage(imageBytes));
        currentItem = product;
    }

    @FXML
    void removeItem(ActionEvent event) {
        Platform.runLater(() -> statusLabel.setText("Item removed"));
        Platform.runLater(()->updateButton.setDisable(true));
        Platform.runLater(()->removeItemButton.setDisable(true));
        try {
            SimpleClient.getClient().sendToServer("REMOVE_ITEM:" + id);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addToCart(ActionEvent event) {
        SimpleClient.addToCart(currentItem, amountSpinner.getValue());
        Platform.runLater(()->addedToCartLabel.setText("Item added to cart"));
        Platform.runLater(()->addedToCartLabel.setStyle("-fx-text-fill: green"));
    }

}

