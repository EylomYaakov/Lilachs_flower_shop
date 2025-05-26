package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.ChangePriceEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.InitDescriptionEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Product;
import javafx.application.Platform;
import org.greenrobot.eventbus.EventBus;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleClient extends AbstractClient {

    public static String sign;
    public static PrimaryController primaryController;
    public static CatalogController catalogController;
    public static ItemController itemController;
    private static SimpleClient client = null;
    private int lastItemId;

    public void setLastItemId(int lastItemId) {
        this.lastItemId = lastItemId;
    }

    public int getLastItemId() {
        return lastItemId;
    }

    private SimpleClient(String host, int port) throws IOException {
        super(host, port);
    }

    public static SimpleClient getClient() throws IOException {
        if (client == null) {
            client = new SimpleClient("localhost", 3000);
        }
        return client;
    }

    @Override
    protected void handleMessageFromServer(Object msg){
        if (msg instanceof InitDescriptionEvent){
            InitDescriptionEvent event = (InitDescriptionEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if (msg instanceof ChangePriceEvent){
            ChangePriceEvent event = (ChangePriceEvent) msg;
            EventBus.getDefault().post(event);
        }
        else if (msg instanceof List<?>){
            List<Product> items = (List<Product>) msg;
            EventBus.getDefault().post(items);
        }
    }

//    private void handleSignAssignment(String message) {
//        if (message.contains("X")) {
//            sign = "X";
//            System.out.println(">> You are assigned: X");
//        } else if (message.contains("O")) {
//            sign = "O";
//            System.out.println(">> You are assigned: O");
//        }
//
//        Platform.runLater(() -> primaryController.disableBoard());
//    }
//
//    private void handleStartPermission() {
//        System.out.println(">> Game ready: both players connected.");
//        if ("X".equals(sign)) {
//            Platform.runLater(() -> primaryController.enableBoard());
//        }
//    }

//    private void parseCompactMove(String message) {
//        try {
//            String movePart = message.substring(0, message.length() - 1);
//            int moveNumber = Integer.parseInt(message.substring(message.length() - 1));
//            String[] parts = movePart.split(",");
//
//            int row = Integer.parseInt(parts[0]);
//            int col = Integer.parseInt(parts[1].substring(0, 1));
//            String sgn = parts[1].substring(1);
//
//            EventBus.getDefault().post(new MoveEvent(row, col, sgn, moveNumber));
//        } catch (Exception e) {
//            System.err.println("!! Could not parse compact move: " + e.getMessage());
//        }
//    }
//
//    private void parseVerboseMove(String message) {
//        try {
//            String[] parts = message.split("there is");
//            String[] pos = parts[0].trim().split(",");
//            int row = Integer.parseInt(pos[0].trim());
//            int col = Integer.parseInt(pos[1].trim().substring(0, 1));
//            String sign = parts[1].split("and the move is")[0].trim();
//            int move = Integer.parseInt(parts[1].split("and the move is")[1].trim());
//
//            EventBus.getDefault().post(new MoveEvent(row, col, sign, move));
//        } catch (Exception e) {
//            System.err.println("!! Could not parse verbose move: " + e.getMessage());
//        }
//    }
}

