package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;

public class CatalogController {

    @FXML
    private Button addItemButton;

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
    private Label statusLabel;

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
    private TextField discount;

    @FXML
    private Label discountLabel;

    @FXML
    private Button leftArrow;

    @FXML
    private Button saleButton;

    @FXML
    private Button startSaleButton;

    @FXML
    private Button rightArrow;

    @FXML
    private ImageView img1;

    @FXML
    private ImageView img2;

    @FXML
    private ImageView img3;

    @FXML
    private ImageView img4;

    @FXML
    private ImageView img5;

    @FXML
    private ImageView img6;

    private Button[] buttons;
    private TextArea[] texts;
    private int[] ids;
    private ImageView[] images;
    private List<Product> products = new ArrayList<>();
    private int currentPage = 0;
    private final int pageSize = 6;
    private boolean chooseItems = false;
    private List<Product> saleProducts = new ArrayList<>();
    private boolean[] isSalePressed;

    @FXML
    void buttonPressed(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == clicked) {
                if(!chooseItems) {
                    try{
                        SimpleClient.getClient().setLastItemId(ids[i]);
                        App.setRoot("item");
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
                else{
                    if(clicked.getStyle().startsWith("-fx-border-color: red")){
                        clicked.setStyle("");
                        saleProducts.remove(products.get(currentPage*pageSize+i));
                        isSalePressed[currentPage*pageSize+i] = false;
                    }
                    else {
                        saleProducts.add(products.get(currentPage*pageSize+i));
                        clicked.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        isSalePressed[currentPage*pageSize+i] = true;
                    }
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
        images = new ImageView[]{img1, img2, img3, img4, img5, img6};
        ids = new int[buttons.length];
        EventBus.getDefault().register(this);
        Platform.runLater(()->discountLabel.setVisible(false));
        Platform.runLater(()->startSaleButton.setVisible(false));
        Platform.runLater(()->discount.setVisible(false));
        try{
            SimpleClient.getClient().sendToServer("GET_CATALOG");
            if (!SimpleClient.getClient().getAccountType().isEmpty()) {
                Platform.runLater(()->loginButton.setText("log out"));
            }
            else{
                Platform.runLater(()->loginButton.setText("log in"));
            }
            if(!SimpleClient.getClient().getAccountType().equals("worker")){
                Platform.runLater(()-> addItemButton.setVisible(false));
                Platform.runLater(()-> saleButton.setVisible(false));
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private String getDetails(Product product){
        String details =  product.name + "\ntype: " + product.type + "\n" + "price: " + product.price;
        if(product.sale > 0){
            details = product.sale + "% sale!\n" + details;
        }
        return details;
    }

    @Subscribe
    public void initCatalog(List<Product> products){
        this.products = products;
        isSalePressed = new boolean[products.size()];
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
            Platform.runLater(()->buttons[finalI].setStyle(""));
            Platform.runLater(()->texts[finalI].setText(getDetails(products[finalI])));
            ids[finalI] = products[finalI].id;
            Platform.runLater(()->texts[finalI].setEditable(false));
            Image image = new Image(new ByteArrayInputStream(products[finalI].image));
            Platform.runLater(()->images[finalI].setImage(image));
            if(isSalePressed[currentPage*pageSize+i]){
                Platform.runLater(()->buttons[finalI].setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;"));
            }

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
        currentPage--;
        for(int i = 0; i < pageSize; i++){
            int finalI = i;
            prevPage[i] = products.get(i + (currentPage)*pageSize);
            Platform.runLater(()->buttons[finalI].setVisible(true));
            Platform.runLater(()->texts[finalI].setVisible(true));

        }
        initPage(prevPage);
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

    @FXML
    void addItem(ActionEvent event) {
        try {
            App.setRoot("addItem");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void showSaleOptions(ActionEvent event) {
        chooseItems = !chooseItems;
        Platform.runLater(()->discountLabel.setVisible(chooseItems));
        Platform.runLater(()->startSaleButton.setVisible(chooseItems));
        Platform.runLater(()->discount.setVisible(chooseItems));
        if(chooseItems){
            saleButton.setText("hide sale options");
            discount.setText("");
            saleProducts.clear();
        }
        else{
            saleButton.setText("show sale options");
            isSalePressed = new boolean[products.size()];
            for(int i = 0; i < buttons.length; i++){
                int finalI = i;
                Platform.runLater(()->buttons[finalI].setStyle(""));
            }
        }
    }

    @FXML
    void startSale(ActionEvent event) {
        int discountAmount = 0;
        try{
            discountAmount = Integer.parseInt(discount.getText());
        }
        catch(NumberFormatException e){
            statusLabel.setText("Invalid discount amount");
            statusLabel.setStyle("-fx-text-fill: red");
            return;
        }
        if(discountAmount < 1 || discountAmount > 99){
            statusLabel.setText("Invalid discount amount");
            statusLabel.setStyle("-fx-text-fill: red");
            return;
        }
        String saleString = "SALE:" + discountAmount;
        for(int i = 0; i < saleProducts.size() ; i++){
            saleString += ":" + saleProducts.get(i).id;
        }
        try{
            //format: SALE:saleAmount:id1:id2:...
            SimpleClient.getClient().sendToServer(saleString);
            statusLabel.setText("Sale started!");
            statusLabel.setStyle("-fx-text-fill: green");
            saleProducts.clear();
            chooseItems = false;
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
