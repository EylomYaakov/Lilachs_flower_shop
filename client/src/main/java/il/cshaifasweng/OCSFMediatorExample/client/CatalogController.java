package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.event.ActionEvent;
import javafx.application.Platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CatalogController {

    @FXML
    private Button menuButton;

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
    private Button filterButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label emptyCatalog;


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

    @FXML
    private Label saleTimeLabel;

    @FXML
    private Spinner<Integer> timeAmountSpinner;

    @FXML
    private ComboBox<String> timeList;


    private Button[] buttons;
    private TextArea[] texts;
    private int[] ids;
    private ImageView[] images;
    private List<Product> products = new ArrayList<>();
    private boolean chooseItems = false;
    private final List<Product> saleProducts = new ArrayList<>();
    private List<Boolean> isSalePressed;
    private static List<String> shops;
    private Paginator<Product> paginator;


    public static List<String> getShops(){
        return shops;
    }

    @FXML
    void buttonPressed(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        int currentIndex = paginator.getCurrentIndex();
        int currentPageSize = paginator.getCurrentPageSize();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == clicked) {
                if(!chooseItems) {
                    try {
                        SimpleClient.setLastShop(shopsFilter.getSelectionModel().getSelectedItem());
                        SimpleClient.getClient().setLastItemId(ids[i]);
                        App.switchScreen("item");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    if(clicked.getStyle().startsWith("-fx-border-color: red")){
                        clicked.setStyle("");
                        saleProducts.remove(paginator.getItem(i));
                        isSalePressed.set(currentIndex-currentPageSize+i, false);
                    }
                    else {
                        saleProducts.add(paginator.getItem(i));
                        clicked.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        isSalePressed.set(currentIndex-currentPageSize+i,true);
                    }
                }
            }
        }

    }

    @FXML
    void loginPressed(ActionEvent event) {
        try {
            if(loginButton.getText().equals("log in")){
                App.switchScreen("login");
            }
            else{
                Platform.runLater(()->loginButton.setText("log in"));
                SimpleClient.getClient().sendToServer("LOGOUT:" + SimpleClient.getUser().getUsername());
                SimpleClient.setRole("");
                SimpleClient.setUser(null);
                initialize();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSaleVisibility(boolean visible){
        Platform.runLater(()->discountLabel.setVisible(visible));
        Platform.runLater(()->startSaleButton.setVisible(visible));
        Platform.runLater(()->discount.setVisible(visible));
        Platform.runLater(()->chooseAllButton.setVisible(visible));
        Platform.runLater(()->saleTimeLabel.setVisible(visible));
        Platform.runLater(()->timeAmountSpinner.setVisible(visible));
        Platform.runLater(()->timeList.setVisible(visible));
        Platform.runLater(()->statusLabel.setVisible(visible));

    }

    @FXML
    public void initialize() {
        buttons = new Button[]{btn1, btn2, btn3, btn4, btn5, btn6};
        texts = new TextArea[]{txt1, txt2, txt3, txt4, txt5, txt6};
        images = new ImageView[]{img1, img2, img3, img4, img5, img6};
        ids = new int[buttons.length];
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        timeAmountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100));
        Platform.runLater(()->timeList.getItems().addAll("Minutes", "Hours", "Days"));
        Platform.runLater(()-> timeList.getSelectionModel().select("Hours"));
        setSaleVisibility(false);
        try{
            SimpleClient.getClient().sendToServer("GET_CATALOG");
            if (!SimpleClient.getRole().isEmpty()) {
                Platform.runLater(()->loginButton.setText("log out"));
            }
            else{
                Platform.runLater(()->loginButton.setText("log in"));
                Platform.runLater(()->menuButton.setVisible(false));
            }
            if(!SimpleClient.getRole().startsWith("worker")){
                Platform.runLater(()-> saleButton.setVisible(false));
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    private String getDetails(Product product){
        String details =  product.name + "\ntype: " + product.type + "\n" + "price: " + product.price;
        if(product.sale > 0) {
            details = product.sale + "% sale!\n" + details;
        }
        return details;
    }




    public void initShops(){
        Set<String> shopsSet = new LinkedHashSet<>();
        for(Product p : products){
            if(!p.shop.equals("all chain")) {
                shopsSet.add(p.shop);
            }
        }
        if(SimpleClient.getRole().startsWith("worker") && !SimpleClient.getRole().contains("shop")) {
            Platform.runLater(()->shopsFilter.getItems().add("all chain"));
        }
        Platform.runLater(()->shopsFilter.getItems().addAll(shopsSet));
        shops = new ArrayList<>(shopsSet);
        if(SimpleClient.getUser() != null) {
            String shop = SimpleClient.getUser().getShop();
            if(!shop.equals("all chain")){
                Platform.runLater(()->shopsFilter.setVisible(false));
                Platform.runLater(()->filterButton.setLayoutX(176));
            }
            else{
                shop = shops.get(0);
            }
            if(!SimpleClient.getLastShop().isEmpty()) {
                shop = SimpleClient.getLastShop();
            }
            String finalShop  = shop;
            Platform.runLater(()-> shopsFilter.getSelectionModel().select(finalShop));
            Platform.runLater(()-> filter(new ActionEvent()));
        }
    }

    @Subscribe
    public void initCatalog(ProductListEvent event){
        List<Product> products = event.getProducts();
        if(products.get(0) == null){
            return;
        }
        this.products = products;
        isSalePressed = Utils.initList(products.size(), false);
        int pageSize = 6;
        paginator = new Paginator<>(products, pageSize);
        renderPage();
        Platform.runLater(()->typesFilter.getItems().add("all items"));
        Set<String> types = new HashSet<>();
        for(Product p : products){
            types.add(p.type);
        }
        Platform.runLater(()->typesFilter.getItems().addAll(types));
        initShops();
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


    private void refreshPage(){
        paginator.setCurrentIndex(paginator.getCurrentIndex() - paginator.getCurrentPageSize());
        renderPage();
    }

    @Subscribe
    public void addProduct(AddProductEvent event){
        Product product = event.getProduct();
        List<String> types = typesFilter.getItems();
        if(!types.contains(product.type)){
            Platform.runLater(()->typesFilter.getItems().add(product.type));
        }
        products.add(product);
        paginator.updateShowProducts();
        isSalePressed.add(false);
        refreshPage();
    }

    @Subscribe
    public void removeProduct(RemoveProductEvent event){
        int id = event.getProductId();
        Product product = Utils.getProductByID(products, id);
        int index = products.indexOf(product);
        products.remove(product);
        paginator.updateShowProducts();
        isSalePressed.remove(index);
        refreshPage();
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        paginator.prevPage();
        renderPage();
    }


    @FXML
    void rightArrowPressed(ActionEvent event){
        renderPage();
    }


    @FXML
    void toMenu(ActionEvent event) {
        SimpleClient.setLastShop(shopsFilter.getSelectionModel().getSelectedItem());
        App.switchScreen("menu");
    }


    @FXML
    void showSaleOptions(ActionEvent event) {
        chooseItems = !chooseItems;
        setSaleVisibility(chooseItems);
        if(chooseItems){
            Platform.runLater(()->saleButton.setText("hide sale options"));
            Platform.runLater(()->discount.setText(""));
            saleProducts.clear();
        }
        else{
            Platform.runLater(()->saleButton.setText("show sale options"));
            isSalePressed = Utils.initList(products.size(), false);
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
        String timeUnit = timeList.getValue();
        int timeAmount = timeAmountSpinner.getValue();
        LocalDateTime saleEnd = switch (timeUnit) {
            case "Minutes" -> LocalDateTime.now().plusMinutes(timeAmount);
            case "Hours" -> LocalDateTime.now().plusHours(timeAmount);
            case "Days" -> LocalDateTime.now().plusDays(timeAmount);
            default -> LocalDateTime.now();
        };
        Sale sale = new Sale(saleProducts, discountAmount, saleEnd);
        try{
            SimpleClient.getClient().sendToServer(sale);
            statusLabel.setText("Sale started!");
            statusLabel.setStyle("-fx-text-fill: green");
            saleProducts.clear();
            Platform.runLater(()->chooseAllButton.setText("choose all items"));
            Collections.fill(isSalePressed, false);
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
        if(chosenType == null && chosenShop == null){
            return;
        }
        else if(chosenType == null){
           chosenType = "all items";
        }
        else if(chosenShop == null){
            chosenShop = "all chain";
        }
        String currentCartShop = getCartShop();
        if(!currentCartShop.equals("all chain") && !currentCartShop.equals(chosenShop)){
            SimpleClient.clearCart();
        }
        for(int i = 0; i < products.size(); i++){
            if((products.get(i).type.equals(chosenType) || chosenType.equals("all items")) && (products.get(i).shop.equals(chosenShop) || chosenShop.equals("all chain") || products.get(i).shop.equals("all chain"))){
               paginator.changeShowProducts(i, true);
            }
            else{
                paginator.changeShowProducts(i, false);
            }
        }
        paginator.reset();
        renderPage();
    }

    @FXML
    void chooseAll(ActionEvent event) {
        String style;
        if(chooseAllButton.getText().equals("choose all items")){
            Platform.runLater(()->chooseAllButton.setText("reset choice"));
            for(int i = 0; i < isSalePressed.size(); i++){
                if(paginator.getShowProducts(i)){
                    isSalePressed.set(i, true);
                    Product p  = products.get(i);
                    if(!saleProducts.contains(p)){
                        saleProducts.add(p);
                    }
                }
            }
            style = "-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;";
        }
        else{
            Platform.runLater(()->chooseAllButton.setText("choose all items"));
            saleProducts.clear();
            Collections.fill(isSalePressed, false);
            style = "";
        }
        Utils.setStyleAllButtons(buttons, style);

    }

    public void renderPage(){
        List<Product> pageItems = paginator.getCurrentPageItems();
        if(pageItems.isEmpty()){
            Platform.runLater(() -> emptyCatalog.setVisible(true));
        }
        else{
            Platform.runLater(()->emptyCatalog.setVisible(false));
        }
        for(int i=0; i<buttons.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setProductVisibility(i,true);
                Platform.runLater(()->buttons[finalI].setStyle(""));
                Platform.runLater(()->texts[finalI].setText(getDetails(pageItems.get(finalI))));
                ids[i] = pageItems.get(i).id;
                Image image = new Image(new ByteArrayInputStream(pageItems.get(finalI).image));
                Platform.runLater(()->images[finalI].setImage(image));
                if(isSalePressed.get(products.indexOf(paginator.getItem(i)))){
                    Platform.runLater(()->buttons[finalI].setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;"));
                }
            }
            else{
                setProductVisibility(i,false);
            }
        }
        Platform.runLater(()->rightArrow.setVisible(paginator.hasNextPage()));
        Platform.runLater(()->leftArrow.setVisible(paginator.hasPreviousPage()));
    }

    public void setProductVisibility(int index, boolean value){
        Platform.runLater(()->buttons[index].setVisible(value));
        Platform.runLater(()->texts[index].setVisible(value));
        Platform.runLater(()->images[index].setVisible(value));
    }

    private String getCartShop(){
       for(BaseProduct baseProduct: SimpleClient.getCart().keySet()){
           if(baseProduct instanceof Product){
               Product product = (Product) baseProduct;
               if(!product.shop.equals("all chain")){
                   return product.shop;
               }
           }
       }
       return "all chain";
    }
}
