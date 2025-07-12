package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import javafx.application.Platform;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

    private static boolean isValidImagePath(String imagePath) {
        Path path = Paths.get(imagePath);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            return false;
        }
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            return img != null;
        } catch (IOException e) {
            return false;
        }
    }

    @FXML
    void addItem(ActionEvent event) {
        String productName = name.getText();
        String productDescription = description.getText();
        String productImagePath = imagePath.getText();
        String ProductType = type.getText();
        String ProductPrice = price.getText();
        double productPriceDouble = 0;
        byte[] image = null;
        if(productName.isEmpty() || productDescription.isEmpty() || productImagePath.isEmpty() || ProductType.isEmpty() || ProductPrice.isEmpty()) {
            Platform.runLater(()->statusLabel.setText("Please fill all the fields"));
            return;
        }
        try{
            productPriceDouble = Double.parseDouble(ProductPrice);
            if(productPriceDouble < 0) {
                Platform.runLater(()->statusLabel.setText("Invalid price"));
                return;
            }
        }
        catch (NumberFormatException e) {
            Platform.runLater(()->statusLabel.setText("Invalid price"));
            return;
        }
        if(!isValidImagePath(productImagePath)) {
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
        Product product = new Product(-1 ,productName, ProductType, productDescription, productPriceDouble, image);
        try {
            SimpleClient.getClient().sendToServer(product);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Platform.runLater(()->statusLabel.setText("Product added"));
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

