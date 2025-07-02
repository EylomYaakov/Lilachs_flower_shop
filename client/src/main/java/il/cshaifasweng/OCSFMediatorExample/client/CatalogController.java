package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
    private Button loginButton;

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

    @FXML
    private Button leftArrow;

    @FXML
    private Button rightArrow;

    private Button[] buttons;
    private TextArea[] texts;
    private int[] ids;
    private List<Product> products = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 6;

    @FXML
    void buttonPressed(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        for(int i = 0; i < buttons.length; i++){
            if(buttons[i] == clicked){
                try {
                    SimpleClient.getClient().setLastItemId(ids[i]);
                    App.setRoot("item");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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


    public void initialize() {
        buttons = new Button[]{btn1, btn2, btn3, btn4, btn5, btn6};
        texts = new TextArea[]{txt1, txt2, txt3, txt4, txt5, txt6};
        ids = new int[buttons.length];
        EventBus.getDefault().register(this);
        try{
            SimpleClient.getClient().sendToServer("GET_CATALOG");
            if (!SimpleClient.getClient().getAccountType().isEmpty()) {
                Platform.runLater(()->loginButton.setText("log out"));
            }
            else{
                Platform.runLater(()->loginButton.setText("log in"));
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private String getDetails(Product product){
        return "type: " + product.type + "\n" + "price: " + product.price;
    }

    @Subscribe
    public void initCatalog(List<Product> products){
        this.products = products;
        int firstPageSize = Math.min(products.size(), pageSize);
        Product[] firstPage = new Product[firstPageSize];
        for(int i = 0; i < firstPageSize; i++){
            firstPage[i] = products.get(i);
        }
        initPage(firstPage);
        Platform.runLater(()->leftArrow.setVisible(false));
        if(products.size() <= pageSize){
            Platform.runLater(()->rightArrow.setVisible(false));
        }
    }

    private void initPage(Product[] products){
        for(int i=0; i<products.length; i++){
            int finalI = i;
            Platform.runLater(()->buttons[finalI].setText(products[finalI].name));
            Platform.runLater(()->texts[finalI].setText(getDetails(products[finalI])));
            Platform.runLater(()->ids[finalI] = products[finalI].id);
            Platform.runLater(()->texts[finalI].setEditable(false));
        }
        for(int i = products.length; i < pageSize; i++){
            int finalI = i;
            Platform.runLater(()->buttons[finalI].setVisible(false));
            Platform.runLater(()->texts[finalI].setVisible(false));
            Platform.runLater(()->rightArrow.setVisible(false));
        }
    }

    @Subscribe
    public void updateDetails(ChangePriceEvent event){
        Product product = event.getProduct();
        for(int i=0; i<ids.length; i++){
            if(ids[i] == product.id){
                int finalI = i;
                Platform.runLater(()->texts[finalI].setText(getDetails(product)));
            }
        }
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        Product[] prevPage = new Product[pageSize];
        for(int i = 0; i < pageSize; i++){
            int finalI = i;
            prevPage[i] = products.get(i + (currentPage-1)*pageSize);
            Platform.runLater(()->buttons[finalI].setVisible(true));
            Platform.runLater(()->texts[finalI].setVisible(true));

        }
        initPage(prevPage);
        currentPage--;
        if(currentPage != 0){
            Platform.runLater(()->leftArrow.setVisible(true));
        }
        else{
            Platform.runLater(()->leftArrow.setVisible(false));
        }
        Platform.runLater(()->rightArrow.setVisible(true));
    }

    @FXML
    void rightArrowPressed(ActionEvent event) {
        int nextPageSize = Math.min(products.size() - (currentPage+1)*pageSize, pageSize);
        Product[] nextPage = new Product[nextPageSize];
        currentPage++;
        for(int i = 0; i < nextPageSize; i++){
            nextPage[i] = products.get(i + currentPage*pageSize);
        }
        initPage(nextPage);
        Platform.runLater(()->leftArrow.setVisible(true));
    }


}
