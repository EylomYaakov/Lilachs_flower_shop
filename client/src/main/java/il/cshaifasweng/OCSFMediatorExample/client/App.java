package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * JavaFX App
 */
public class App extends Application {
    private ListView<String> catalogList;
    private Label itemDetails;
    private static Scene scene;
    private SimpleClient client;

    @Override
    public void start(Stage stage) throws IOException {
        EventBus.getDefault().register(this);
        client = SimpleClient.getClient();
        client.openConnection();
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();


        try {
            client = SimpleClient.getClient();
            client.openConnection();
            System.out.println("✅ Connected to server");

            // Send static test requests
            client.sendToServer("GET_CATALOG");
            Thread.sleep(500);  // wait for the response

            client.sendToServer("GET_ITEM:1");
            Thread.sleep(500);  // wait for the response

            // Optional: close connection right after testing
            client.closeConnection();
            System.out.println("❌ Disconnected");

            // Exit JavaFX platform cleanly
            Platform.exit();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        catalogList = new ListView<>();
        itemDetails = new Label("Select an item to view details.");

        catalogList.setOnMouseClicked(event -> {
            String selected = catalogList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String id = selected.split(",")[0];
                try {
                    client.sendToServer("GET_ITEM:" + id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Button refreshButton = new Button("Refresh Catalog");
        refreshButton.setOnAction(e -> {
            try {
                client.sendToServer("GET_CATALOG");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        VBox layout = new VBox(10, refreshButton, catalogList, itemDetails);
        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.setTitle("🌱 Plant Shop Client");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }



    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        EventBus.getDefault().unregister(this);
        //client.sendToServer("remove client");
        client.closeConnection();
        super.stop();
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING,
                    String.format("Message: %s\nTimestamp: %s\n",
                            event.getWarning().getMessage(),
                            event.getWarning().getTime().toString())
            );
            alert.show();
        });

    }

    public static void main(String[] args) {
        launch();
    }

}