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
    private boolean[] isSalePressed;
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
                        saleProducts.remove(products.get(currentIndex-currentPageSize+i));
                        isSalePressed[currentIndex-currentPageSize+i] = false;
                    }
                    else {
                        saleProducts.add(products.get(currentIndex-currentPageSize+i));
                        clicked.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        isSalePressed[currentIndex-currentPageSize+i] = true;
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
        EventBus.getDefault().register(this);
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
            if(!SimpleClient.getRole().equals("worker")){
                Platform.runLater(()-> saleButton.setVisible(false));
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }

        //for shops to be not empty
        List<String> shops = new ArrayList<>();
        shops.add("shop1");
        shops.add("shop2");
        shops.add("shop3");
        ShopsListEvent event = new ShopsListEvent(shops);
        initShops(event);

    }

    private String getDetails(Product product){
        String details =  product.name + "\ntype: " + product.type + "\n" + "price: " + product.price;
        if(product.sale > 0){
            details = product.sale + "% sale!\n" + details;
        }
        return details;
    }




    @Subscribe
    public void initShops(ShopsListEvent event){
        List<String> shops = event.getShops();
        CatalogController.shops = shops;
        Platform.runLater(()->shopsFilter.getItems().add("all chain"));
        for(String shop : shops){
            Platform.runLater(()->shopsFilter.getItems().add(shop));
        }
    }

    @Subscribe
    public void initCatalog(ProductListEvent event){
        List<Product> products = event.getProducts();
        if(products.get(0) == null){
            return;
        }
        this.products = products;
        isSalePressed = new boolean[products.size()];
        int pageSize = 6;
        paginator = new Paginator<>(products, pageSize);
        if(SimpleClient.getUser() != null && SimpleClient.getUser().getAccountType().startsWith("shop")){
            String shop = SimpleClient.getUser().getAccountType().substring(14);
            for(int i=0; i<products.size(); i++){
                if(!products.get(i).shop.equals(shop) && !products.get(i).shop.equals("all chain")){
                    paginator.changeShowProducts(i, false);
                }
                Platform.runLater(()->shopsFilter.setVisible(false));
                Platform.runLater(()->filterButton.setLayoutX(176));

            }
        }
        renderPage();
        Platform.runLater(()->typesFilter.getItems().add("all items"));
        Set<String> types = new HashSet<>();
        for(Product p : products){
            types.add(p.type);
        }
        for(String type : types){
            Platform.runLater(()->typesFilter.getItems().add(type));
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
        paginator.prevPage();
        renderPage();
    }


    @FXML
    void rightArrowPressed(ActionEvent event){
        renderPage();
    }


    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }


    @FXML
    void showSaleOptions(ActionEvent event) {
        chooseItems = !chooseItems;
        System.out.println(chooseItems);
        setSaleVisibility(chooseItems);
        if(chooseItems){
            Platform.runLater(()->saleButton.setText("hide sale options"));
            Platform.runLater(()->discount.setText(""));
            saleProducts.clear();
        }
        else{
            Platform.runLater(()->saleButton.setText("show sale options"));
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
        String timeUnit = timeList.getValue();
        int timeAmount = timeAmountSpinner.getValue();
        LocalDateTime saleEnd = switch (timeUnit) {
            case "Minutes" -> LocalDateTime.now().plusMinutes(timeAmount);
            case "Hours" -> LocalDateTime.now().plusHours(timeAmount);
            case "Days" -> LocalDateTime.now().plusDays(timeAmount);
            default -> LocalDateTime.now();
        };
        Sale sale = new Sale(products, discountAmount, saleEnd);
        try{
            SimpleClient.getClient().sendToServer(sale);
            statusLabel.setText("Sale started!");
            statusLabel.setStyle("-fx-text-fill: green");
            saleProducts.clear();
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
        if(chosenType == null && chosenShop == null){
            return;
        }
        else if(chosenType == null){
           chosenType = "all items";
        }
        else if(chosenShop == null){
            chosenShop = "all chain";
        }
        for(int i = 0; i < products.size(); i++){
            if((products.get(i).type.equals(chosenType) || chosenType.equals("all items")) && (products.get(i).shop.equals(chosenShop) || products.get(i).shop.equals("all chain"))){
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

    public void renderPage(){
        List<Product> pageItems = paginator.getCurrentPageItems();
        for(int i=0; i<buttons.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setProductVisibility(i,true);
                Platform.runLater(()->buttons[finalI].setStyle(""));
                Platform.runLater(()->texts[finalI].setText(getDetails(pageItems.get(finalI))));
                ids[i] = pageItems.get(i).id;
                Image image = new Image(new ByteArrayInputStream(pageItems.get(finalI).image));
                Platform.runLater(()->images[finalI].setImage(image));
                if(isSalePressed[paginator.getCurrentIndex()-pageItems.size()+i]){
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

}
