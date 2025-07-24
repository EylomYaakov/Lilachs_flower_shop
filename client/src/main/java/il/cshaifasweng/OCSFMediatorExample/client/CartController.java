package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.BaseProduct;
import il.cshaifasweng.OCSFMediatorExample.entities.CustomProduct;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.util.StringConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class CartController {

    @FXML
    private TextField address;

    @FXML
    private Label addressLabel;

    @FXML
    private Label emptyCart;

    @FXML
    private Label addProductStatus;

    @FXML
    private ComboBox<String> colorsList;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField grettingCard;

    @FXML
    private Spinner<Integer> hoursSpinner;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;

    @FXML
    private Label statusLabel;

    @FXML
    private Button leftArrow;

    @FXML
    private Button placeOrderButton;

    @FXML
    private Spinner<Integer> minutesSpinner;

    @FXML
    private TextField name;

    @FXML
    private Label nameLabel;

    @FXML
    private TextField phoneNumber;

    @FXML
    private Label phoneNumberLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private ComboBox<String> priceRangesList;

    @FXML
    private Button removeButton1;

    @FXML
    private Button removeButton2;

    @FXML
    private Button removeButton3;

    @FXML
    private Button rightArrow;

    @FXML
    private Spinner<Integer> spinner1;

    @FXML
    private Spinner<Integer> spinner2;

    @FXML
    private Spinner<Integer> spinner3;

    @FXML
    private CheckBox deliveryCheckBox;

    @FXML
    private ComboBox<String> typesList;

    private int pageSize = 3;
    private Map<BaseProduct, Integer> cart = new LinkedHashMap<>();

    private Paginator<BaseProduct> paginator;
    private Label[] productLabels;
    private Spinner<Integer>[] amountSpinners;
    private Button[] removeButtons;

    @FXML
    public void initialize(){
        cart = SimpleClient.getCart();
        Platform.runLater(()->typesList.getItems().addAll("flower arrangement", "Flowering potted plant", "bridal bouquet", "flowers cluster"));
        Platform.runLater(()->priceRangesList.getItems().addAll("10₪-50₪", "50₪-100₪", "100₪-150₪", "150₪-250₪", "250₪-350₪", "350₪-500₪"));
        Platform.runLater(()->colorsList.getItems().addAll("red", "green", "blue", "yellow", "white", "purple", "pink", "orange"));
        spinner1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000));
        spinner2.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000));
        spinner3.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000));
        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23));
        minutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        productLabels = new Label[]{label1, label2, label3};
        amountSpinners = new Spinner[]{spinner1, spinner2, spinner3};
        removeButtons = new Button[]{removeButton1, removeButton2, removeButton3};
        setSpinnersObserver();
        StringConverter<Integer> twoDigitConverter = new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                if (value == null) return "00";
                return String.format("%02d", value);
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        };
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable all past dates (before today)
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #dddddd;"); // optional gray out
                }
            }
        });
        hoursSpinner.getValueFactory().setConverter(twoDigitConverter);
        minutesSpinner.getValueFactory().setConverter(twoDigitConverter);
        hoursSpinner.getValueFactory().setValue(LocalTime.now().getHour());
        minutesSpinner.getValueFactory().setValue(LocalTime.now().getMinute());
        List<BaseProduct> products = new ArrayList<>(cart.keySet());
        paginator = new Paginator<>(products, pageSize);
        renderPage();
        changePrice();
    }

    private void setSpinnersObserver(){
        for(int i = 0; i<amountSpinners.length; i++){
            int finalI = i;
            amountSpinners[i].valueProperty().addListener((observable, oldValue, newValue) -> {spinnerChanged(amountSpinners[finalI], newValue);});
        }
    }

    private BaseProduct getProduct(int index){
        List<BaseProduct> products = new ArrayList<>(cart.keySet());
        return products.get(paginator.getCurrentIndex()-paginator.getCurrentPageSize()+index);
    }

    private void spinnerChanged(Spinner<Integer> source, int newValue){
        for(int i = 0; i<amountSpinners.length; i++){
            if(source == amountSpinners[i]){
                BaseProduct product = getProduct(i);
                cart.put(product, newValue);
                changePrice();
                return;
            }
        }
    }

    @FXML
    void addDelvery(ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();
        boolean checked = checkBox.isSelected();
        addressLabel.setVisible(checked);
        address.setVisible(checked);
        nameLabel.setVisible(checked);
        name.setVisible(checked);
        phoneNumberLabel.setVisible(checked);
        phoneNumber.setVisible(checked);
        changePrice();
    }

    @FXML
    void addGreetingCard(ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();
        boolean checked = checkBox.isSelected();
        grettingCard.setVisible(checked);
    }

    @FXML
    void addProduct(ActionEvent event) {
        String type = typesList.getValue();
        String priceRange = priceRangesList.getValue();
        if(type==null){
            Platform.runLater(()->addProductStatus.setText("Please select product type"));
            Platform.runLater(()->addProductStatus.setStyle("-fx-text-fill: red;"));
            return;
        }
        else if(priceRange==null){
            Platform.runLater(()->addProductStatus.setText("Please select price range"));
            Platform.runLater(()->addProductStatus.setStyle("-fx-text-fill: red;"));
            return;
        }
        String color = colorsList.getValue();
        CustomProduct newProduct = new CustomProduct(type, priceRange, color);
        boolean added = false;
        for(BaseProduct baseProduct: cart.keySet()){
            if(baseProduct instanceof CustomProduct){
                CustomProduct customProduct = (CustomProduct) baseProduct;
                if(customProduct.equals(newProduct)){
                    cart.put(customProduct, cart.get(customProduct)+1);
                    added = true;
                }
            }
        }
        if(!added) {
            cart.put(newProduct, 1);
            paginator.addItem(newProduct);
        }
        refreshPage();
        changePrice();
        Platform.runLater(()->addProductStatus.setText("product added!"));
        Platform.runLater(()->addProductStatus.setStyle("-fx-text-fill: green;"));
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        paginator.prevPage();
        renderPage();
    }

    @FXML
    void placeOrder(ActionEvent event) {
        LocalDate date = datePicker.getValue();
        int hour = hoursSpinner.getValue();
        int minute = minutesSpinner.getValue();
        LocalDateTime dateWithTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
        if(date == null){
            Platform.runLater(()->statusLabel.setText("Please select date"));
            Platform.runLater(()->statusLabel.setStyle("-fx-text-fill: red;"));
            return;
        }
        if(deliveryCheckBox.isSelected()){
            if(address.getText().isEmpty() || name.getText().isEmpty() || phoneNumber.getText().isEmpty()){
                Platform.runLater(()->statusLabel.setText("Please fill all the fields"));
                Platform.runLater(()->statusLabel.setStyle("-fx-text-fill: red;"));
                return;
            }
        }
        if(dateWithTime.isBefore(LocalDateTime.now())){
            Platform.runLater(()->statusLabel.setText("please select a future date"));
            Platform.runLater(()->statusLabel.setStyle("-fx-text-fill: red;"));
            return;
        }
        Order order = new Order(cart, grettingCard.getText(), address.getText(), phoneNumber.getText(), name.getText(), dateWithTime, LocalDate.now(), SimpleClient.getId());
        try{
            SimpleClient.getClient().sendToServer(order);
            statusLabel.setText("Order Placed!");
            statusLabel.setStyle("-fx-text-fill: green");
            cart.clear();
            paginator.clearItems();
            renderPage();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    void removeFromCart(ActionEvent event) {
        Button source = (Button) event.getSource();
        for(int i = 0; i<removeButtons.length; i++){
            if(source == removeButtons[i]){
                BaseProduct product = getProduct(i);
                cart.remove(product);
                paginator.removeItem(product);
                refreshPage();
                changePrice();
                return;
            }
        }
    }

    @FXML
    void rightArrowPressed(ActionEvent event) {
        renderPage();
    }

    private void renderPage(){
        List<BaseProduct> pageItems = paginator.getCurrentPageItems();
        if(pageItems.isEmpty()){
            emptyCart.setVisible(true);
            placeOrderButton.setDisable(true);

        }
        else {
            emptyCart.setVisible(false);
            placeOrderButton.setDisable(false);
        }
        for(int i=0; i<productLabels.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setProductVisibility(i,true);
                Platform.runLater(() -> productLabels[finalI].setText(productLabelLine(pageItems.get(finalI))));
                int count = cart.getOrDefault(pageItems.get(i), 0);
                Platform.runLater(() -> amountSpinners[finalI].getValueFactory().setValue(count));
            }
            else{
                setProductVisibility(i,false);
            }
        }
        Platform.runLater(()->rightArrow.setVisible(paginator.hasNextPage()));
        Platform.runLater(()->leftArrow.setVisible(paginator.hasPreviousPage()));
    }

    private void setProductVisibility(int i, boolean visible){
        Platform.runLater(()->productLabels[i].setVisible(visible));
        Platform.runLater(()->amountSpinners[i].setVisible(visible));
        Platform.runLater(()->removeButtons[i].setVisible(visible));
    }

    private String productLabelLine(BaseProduct product){
        if(product instanceof Product){
            return ((Product)product).name;
        }
        else{
            CustomProduct customProduct = (CustomProduct)product;
            if (customProduct.color != null) {
                return "custom product: " + customProduct.type + ", " + customProduct.priceRange + ", " + customProduct.color;
            }
            return "custom product: " + customProduct.type + ", " + customProduct.priceRange;
        }
    }

    private void refreshPage(){
        paginator.setCurrentIndex(paginator.getCurrentIndex() - paginator.getCurrentPageSize());
        renderPage();
    }


    private void changePrice(){
        double minPrice = 0;
        double maxPrice = 0;
        for (Map.Entry<BaseProduct, Integer> entry : cart.entrySet()){
            BaseProduct baseProduct = entry.getKey();
            int count = entry.getValue();
            if(baseProduct instanceof Product){
                Product product = (Product)baseProduct;
                minPrice += product.price*count;
                maxPrice += product.price*count;
            }
            else{
                CustomProduct customProduct = (CustomProduct)baseProduct;
                String priceRange = customProduct.priceRange;
                int dashIndex = priceRange.indexOf("-");
                minPrice += Integer.parseInt(priceRange.substring(0, dashIndex-1))*count;
                maxPrice += Integer.parseInt(priceRange.substring(dashIndex+1, priceRange.length()-1))*count;
            }
        }
        if(deliveryCheckBox.isSelected()){
            minPrice += 10;
            maxPrice += 10;
        }
        try{
            if(SimpleClient.getUser().getAccountType().equals("subscription")){
                if(minPrice >= 50){
                    minPrice = minPrice*0.9;
                    maxPrice = maxPrice*0.9;
                }
                else if(maxPrice >= 50){
                    maxPrice = maxPrice*0.9;
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        if(minPrice == maxPrice){
            priceLabel.setText("total price: " + minPrice);
        }
        else{
            priceLabel.setText("total price: " + minPrice + " - " + maxPrice);
        }
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

}
