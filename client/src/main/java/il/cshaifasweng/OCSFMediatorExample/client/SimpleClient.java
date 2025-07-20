package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.io.IOException;
import java.util.HashMap;
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
    private String accountType = "worker";
    private int lastItemId;
    private static Map<BaseProduct, Integer> cart = new LinkedHashMap<>();
    private static int accountId;

    public static int getId(){
        return accountId;
    }

    public static Map<BaseProduct, Integer> getCart(){
        return cart;
    }

    public static void addToCart(Product product, int amount){
        cart.merge(product, amount, Integer::sum);
    }

    public void setLastItemId(int lastItemId) {
        this.lastItemId = lastItemId;
    }

    public int getLastItemId() {
        return lastItemId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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
        } else if (msg instanceof ChangePriceEvent) {
            ChangePriceEvent event = (ChangePriceEvent) msg;
            EventBus.getDefault().post(event);
        } else if (msg instanceof List<?>) {
            List<Product> items = (List<Product>) msg;
            EventBus.getDefault().post(items);
        }
        else if (msg instanceof LoginEvent) {
            LoginEvent event = (LoginEvent) msg;
            EventBus.getDefault().post(event);
        }
    }
}
