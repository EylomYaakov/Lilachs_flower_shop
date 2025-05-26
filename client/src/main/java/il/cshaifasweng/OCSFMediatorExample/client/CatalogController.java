package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;

public class CatalogController{

    @FXML
    private Button btn1;

    @FXML
    private Button btn2;

    @FXML
    private Button btn3;

    @FXML
    private Button btn4;

    @FXML
    private Button btn5;

    @FXML
    private Button btn6;

    @FXML
    private TextArea txt1;

    @FXML
    private TextArea txt2;

    @FXML
    private TextArea txt3;

    @FXML
    private TextArea txt4;

    @FXML
    private TextArea txt5;

    @FXML
    private TextArea txt6;

    private Button[] buttons;
    private TextArea[] texts;
    private int[] ids;


    @FXML
    void buttonPressed(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        for(int i = 0; i < buttons.length; i++){
            if(buttons[i] == clicked){
                try {
                    SimpleClient.getClient().setLastItemId(ids[i]);
                    App.setRoot("catalog");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void initialize() {
        buttons = new Button[]{btn1, btn2, btn3, btn4, btn5, btn6};
        texts = new TextArea[]{txt1, txt2, txt3, txt4, txt5, txt6};
        ids = new int[buttons.length];
        EventBus.getDefault().register(this);
    }

    private String getDetails(Product product){
        return "type: " + product.type + "\n" + "price: " + product.price;
    }

    @Subscribe
    public void initCatalog(Product[] products){
        for(int i=0; i<products.length; i++){
            int finalI = i;
            Platform.runLater(()->buttons[finalI].setText(products[finalI].name));
            Platform.runLater(()->texts[finalI].setText(getDetails(products[finalI])));
            Platform.runLater(()->ids[finalI] = products[finalI].id);
        }
    }

    @Subscribe
    public void updateDetails(Product product){
        for(int i=0; i<ids.length; i++){
            if(ids[i] == product.id){
                int finalI = i;
                Platform.runLater(()->texts[finalI].setText(getDetails(product)));
            }
        }
    }




}
