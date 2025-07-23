package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import javafx.application.Platform;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;

public class AddItemController {

    @FXML
    private TextField description;

    @FXML
    private TextField imagePath;

    @FXML
    private TextField name;

    @FXML
    private TextField price;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField type;

    @FXML
    private ComboBox<String> shopsFilter;


    @FXML
    public void initialize() {
        //List<String> shops = CatalogController.getShops();
        Platform.runLater(()-> shopsFilter.getItems().add("all chain"));
//        for(String shop : shops) {
//            shopsFilter.getItems().add(shop);
//        }
    }


    @FXML
    void addItem(ActionEvent event) {
        String productName = name.getText();
        String productDescription = description.getText();
        String productImagePath = imagePath.getText();
        String productType = type.getText();
        String productPrice = price.getText();
        String productShop =  shopsFilter.getSelectionModel().getSelectedItem();
        double productPriceDouble = Utils.getPrice(productPrice);
        byte[] image = null;
        if(productName.isEmpty() || productDescription.isEmpty() || productImagePath.isEmpty() || productType.isEmpty() || productPrice.isEmpty() || productShop == null) {
            Platform.runLater(()->statusLabel.setText("Please fill all the fields"));
            return;
        }
        if(productPriceDouble < 0){
            Platform.runLater(() -> statusLabel.setText("Invalid price"));
            return;
        }
        if(!Utils.isValidImagePath(productImagePath)) {
            Platform.runLater(()->statusLabel.setText("Invalid image path"));
            return;
        }
        try {
            image = Files.readAllBytes(Paths.get(productImagePath));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //server should give an actual id
        Product product = new Product(-1 ,productName, productType, productDescription, productPriceDouble, image);

        try {
            SimpleClient.getClient().sendToServer(product);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(()->statusLabel.setText("Product added"));
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

}

