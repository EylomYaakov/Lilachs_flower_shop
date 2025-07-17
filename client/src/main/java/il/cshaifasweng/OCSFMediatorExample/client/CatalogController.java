package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private ComboBox<String> typesFilter;

    @FXML
    private ComboBox<String> shopsFilter;

    @FXML
    private Button chooseAllButton;

    private Button[] buttons;
    private TextArea[] texts;
    private int[] ids;
    private ImageView[] images;
    private List<Product> products = new ArrayList<>();
    private int currentIndex = 0;
    private final int pageSize = 6;
    private boolean chooseItems = false;
    private List<Product> saleProducts = new ArrayList<>();
    private boolean[] isSalePressed;
    private boolean[] toShow;
    private static List<String> shops;


    public static List<String> getShops(){
        return shops;
    }

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
                        saleProducts.remove(products.get(currentIndex-currentPageSize()+i));
                        isSalePressed[currentIndex-currentPageSize()+i] = false;
                    }
                    else {
                        saleProducts.add(products.get(currentIndex-currentPageSize()+i));
                        clicked.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        isSalePressed[currentIndex-currentPageSize()+i] = true;
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

    @FXML
    public void initialize() {
        buttons = new Button[]{btn1, btn2, btn3, btn4, btn5, btn6};
        texts = new TextArea[]{txt1, txt2, txt3, txt4, txt5, txt6};
        images = new ImageView[]{img1, img2, img3, img4, img5, img6};
        ids = new int[buttons.length];
        EventBus.getDefault().register(this);
        Platform.runLater(()->discountLabel.setVisible(false));
        Platform.runLater(()->startSaleButton.setVisible(false));
        Platform.runLater(()->discount.setVisible(false));
        Platform.runLater(()->chooseAllButton.setVisible(false));
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


    private int productLeftToShow(){
        int count = 0;
        for(int i=currentIndex; i<toShow.length; i++){
            if(toShow[i]){
                count++;
            }
        }
        return count;
    }

    private Product[] getPage(){
        int size = Math.min(productLeftToShow(), pageSize);
        Product[] page = new Product[size];
        int i = 0;
        while(i < size){
            if(toShow[currentIndex]){
                page[i] = products.get(currentIndex);
                i++;
            }
            currentIndex++;
        }
        return page;
    }

    @Subscribe void initShops(List<String> shops){
        CatalogController.shops = shops;
        shopsFilter.getItems().add("all chain");
        for(String shop : shops){
            shopsFilter.getItems().add(shop);
        }
    }

    @Subscribe
    public void initCatalog(List<Product> products){
        this.products = products;
        isSalePressed = new boolean[products.size()];
        toShow = new boolean[products.size()];
        Arrays.fill(toShow, true);
        Product[] firstPage = getPage();
        initPage(firstPage);
        Platform.runLater(()->leftArrow.setVisible(false));
        if(products.size() <= pageSize){
            Platform.runLater(()->rightArrow.setVisible(false));
        }
        typesFilter.getItems().add("all items");
        Set<String> types = new HashSet<>();
        for(Product p : products){
            types.add(p.type);
        }
        for(String type : types){
            typesFilter.getItems().add(type);
        }
    }

    private int currentPageSize(){
        int size = 0;
        for(int i=0; i<buttons.length; i++){
            int finalI = i;
            if(buttons[i].isVisible()){
                size++;
            }
        }
        return size;
    }

    private void initPage(Product[] products){
        for(int i=0; i<products.length; i++){
            int finalI = i;
            Platform.runLater(()->buttons[finalI].setVisible(true));
            Platform.runLater(()->texts[finalI].setVisible(true));
            Platform.runLater(()->images[finalI].setVisible(true));
            Platform.runLater(()->rightArrow.setVisible(true));
            Platform.runLater(()->buttons[finalI].setStyle(""));
            Platform.runLater(()->texts[finalI].setText(getDetails(products[finalI])));
            ids[finalI] = products[finalI].id;
            Platform.runLater(()->texts[finalI].setEditable(false));
            Image image = new Image(new ByteArrayInputStream(products[finalI].image));
            Platform.runLater(()->images[finalI].setImage(image));
           if(isSalePressed[currentIndex-products.length+i]){
                Platform.runLater(()->buttons[finalI].setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;"));
           }

        }
        for(int i = products.length; i < pageSize; i++){
            int finalI = i;
            Platform.runLater(()->buttons[finalI].setVisible(false));
            Platform.runLater(()->texts[finalI].setVisible(false));
            Platform.runLater(()->images[finalI].setVisible(false));
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


    private void findPrevPageStart(){
        int itemCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(toShow[i]){
                itemCount++;
            }
            if(itemCount == pageSize + currentPageSize()){
                currentIndex = i;
                return;
            }
        }
    }

    private boolean isAnotherPrevPage(){
        int beforeCount = 0;
        for(int i=currentIndex-1; i>=0; i--){
            if(toShow[i]){
                beforeCount++;
            }
            if(beforeCount > pageSize){
                return true;
            }
        }
        return false;
    }

    @FXML
    void leftArrowPressed(ActionEvent event){
        for(int i = 0; i<buttons.length; i++){
            if(!buttons[i].isVisible()){
                int finalI = i;
                Platform.runLater(()->buttons[finalI].setVisible(true));
                Platform.runLater(()->texts[finalI].setVisible(true));
                Platform.runLater(()->images[finalI].setVisible(true));
            }
        }
        findPrevPageStart();
        Product[] prevPage = getPage();
        initPage(prevPage);
        if(isAnotherPrevPage()){
            Platform.runLater(()->leftArrow.setVisible(true));
        }
        else{
            Platform.runLater(()->leftArrow.setVisible(false));
        }
        Platform.runLater(()->rightArrow.setVisible(true));
    }



    @FXML
    void rightArrowPressed(ActionEvent event) {
        Product[] nextPage = getPage();
        initPage(nextPage);
        Platform.runLater(()->leftArrow.setVisible(true));
        if(productLeftToShow() == 0){
            Platform.runLater(()->rightArrow.setVisible(false));
        }
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
        Platform.runLater(()->chooseAllButton.setVisible(chooseItems));
        if(chooseItems){
            saleButton.setText("hide sale options");
            discount.setText("");
            saleProducts.clear();
        }
        else{
            saleButton.setText("show sale options");
            isSalePressed = new boolean[products.size()];
            Utils.setStyleAllButtons(buttons, "");
        }
    }

    @FXML
    void startSale(ActionEvent event) {
        int discountAmount = Utils.getDiscount(discount.getText());
        if(saleProducts.isEmpty()){
            statusLabel.setText("please choose items");
            statusLabel.setStyle("-fx-text-fill: red");
            return;
        }
        if(discountAmount < 0){
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
            Arrays.fill(isSalePressed, false);
            Utils.setStyleAllButtons(buttons, "");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    void filter(ActionEvent event) {
        String chosenType =  typesFilter.getSelectionModel().getSelectedItem();
        String chosenShop = shopsFilter.getSelectionModel().getSelectedItem();
        for(int i = 0; i < products.size(); i++){
            if((products.get(i).type.equals(chosenType) || chosenType.equals("all items")) && (products.get(i).shop.equals(chosenShop) || products.get(i).shop.equals("all chain"))){
                toShow[i] = true;
            }
            else{
                toShow[i] = false;
            }
        }
        currentIndex = 0;
        Product[] page = getPage();
        initPage(page);
    }

    @FXML
    void chooseAll(ActionEvent event) {
        String style;
        if(chooseAllButton.getText().equals("choose all items")){
            Platform.runLater(()->chooseAllButton.setText("reset choice"));
            Arrays.fill(isSalePressed, true);
            style = "-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;";
        }
        else{
            Platform.runLater(()->chooseAllButton.setText("choose all items"));
            Arrays.fill(isSalePressed, false);
            style = "";
        }
        Utils.setStyleAllButtons(buttons, style);

    }

}
