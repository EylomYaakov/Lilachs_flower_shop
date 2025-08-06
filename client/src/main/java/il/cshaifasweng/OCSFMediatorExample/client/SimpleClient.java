package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleClient extends AbstractClient {

    public static String sign;
    public static CatalogController catalogController;
    public static ItemController itemController;
    public static LoginController loginController;
    public static SignUpController signUpController;
    private static SimpleClient client = null;
    private static ConnectedUser user = null;
    private int lastItemId;
    private static Map<BaseProduct, Integer> cart = new LinkedHashMap<>();
    private static int accountId;
    private static String lastShop = "";


    public static void setLastShop(String shop) {
        lastShop = shop;
    }

    public static String getLastShop() {
        return lastShop;
    }

    public static int getId(){
        return accountId;
    }

    public static void setId(int id) {
        accountId = id;
    }

    public static Map<BaseProduct, Integer> getCart(){
        return cart;
    }

    public static void clearCart(){
        cart.clear();
    }

    public static void addToCart(Product newProduct, int amount){
        for(BaseProduct baseProduct: cart.keySet()){
            if(baseProduct instanceof Product){
                Product product = (Product) baseProduct;
                if(product.id == newProduct.id){
                    cart.merge(product, amount, Integer::sum);
                    return;
                }
            }
        }
        cart.merge(newProduct, amount, Integer::sum);
    }

    public void setLastItemId(int lastItemId) {
        this.lastItemId = lastItemId;
    }

    public int getLastItemId() {
        return lastItemId;
    }

    public static String getRole() {
        if(user != null){
            return user.getRole();
        }
        return "";
    }


    public static void setUser(ConnectedUser user) {
        SimpleClient.user = user;
    }

    public static void setRole(String accountType) {
        if(user != null){
            user.setRole(accountType);
        }
    }

    public static ConnectedUser getUser() {
      /* if(user == null){
            ConnectedUser user = new ConnectedUser("~", "~", "~", "~", "subscription");
            return user;
        }*/
        return user;
    }

    private SimpleClient(String host, int port) throws IOException {
        super(host, port);
    }

    public static SimpleClient getClient() throws IOException {
        if (client == null) {
            client = new SimpleClient("127.0.0.1", 3000);
        }
        return client;
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof InitDescriptionEvent) {
            InitDescriptionEvent event = (InitDescriptionEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if (msg instanceof ChangePriceEvent) {
            ChangePriceEvent event = (ChangePriceEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if (msg instanceof ProductListEvent) {
            ProductListEvent event = (ProductListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if (msg instanceof LoginEvent) {
            LoginEvent event = (LoginEvent) msg;
            EventBus.getDefault().post(event);

        }
        else if (msg instanceof SignUpEvent) {
            SignUpEvent event = (SignUpEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof ShopsListEvent){
            ShopsListEvent event = (ShopsListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof OrdersListEvent){
            OrdersListEvent event = (OrdersListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof ComplaintsListEvent){
            ComplaintsListEvent event = (ComplaintsListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof ChangeUserDetailsEvent){
            ChangeUserDetailsEvent event = (ChangeUserDetailsEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof AllOrdersEvent){
            AllOrdersEvent event = (AllOrdersEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof AllComplaintsEvent){
            AllComplaintsEvent event = (AllComplaintsEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof UsersListEvent){
            UsersListEvent event = (UsersListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof ConnectedUser) {
            user = (ConnectedUser) msg;
        }
        else if(msg instanceof ChangeUsernameEvent){
            ChangeUsernameEvent event = (ChangeUsernameEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof SubscriptionDatesListEvent){
            SubscriptionDatesListEvent event = (SubscriptionDatesListEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof AddProductEvent){
            AddProductEvent event = (AddProductEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof RemoveProductEvent){
            RemoveProductEvent event = (RemoveProductEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if(msg instanceof Sale){
            Sale sale = (Sale) msg;
            EventBus.getDefault().post(sale);
        }
        else if(msg instanceof Integer){
            accountId = (Integer) msg;
        }
        else if(msg instanceof String){
            String s = (String) msg;
            System.out.println(s);
        }
    }
}
