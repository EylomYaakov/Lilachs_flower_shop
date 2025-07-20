package il.cshaifasweng.OCSFMediatorExample.client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.scene.control.*;

public class Utils {
    public static boolean isValidImagePath(String imagePath) {
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

    public static double getPrice(String price){
        double priceDouble = 0;
        try{
            priceDouble = Double.parseDouble(price);
            if(priceDouble <= 0){
                return -1;
            }
        }
        catch(NumberFormatException e){
            return -1;
        }
        return priceDouble;
    }

    public static int getDiscount(String discount){
        int discountInt = 0;
        try{
            discountInt = Integer.parseInt(discount);
            if(discountInt <= 0 || discountInt >= 100){
                return -1;
            }
        }
        catch(NumberFormatException e){
            return -1;
        }
        return discountInt;
    }

    public static void setStyleAllButtons(Button[] buttons, String style){
        for(Button button : buttons){
            Platform.runLater(()->button.setStyle(style));
        }
    }

}
