package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;

import java.io.IOException;
import java.util.List;

public class SimpleClient extends AbstractClient {

    public static String sign;
    public static CatalogController catalogController;
    public static ItemController itemController;
    public static LoginController loginController;
    public static SignUpController signUpController;
    private static SimpleClient client = null;
    private boolean loggedIn = false;
    private int lastItemId;

    public void setLastItemId(int lastItemId) {
        this.lastItemId = lastItemId;
    }

    public int getLastItemId() {
        return lastItemId;
    }

    public boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
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
    }
}
