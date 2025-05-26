package il.cshaifasweng.OCSFMediatorExample.client;


import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import javafx.fxml.FXMLLoader;

public class Tester extends Application {

    private ListView<String> catalogList;
    private Label itemDetails;
    private Scene scene;
    private SimpleClient client;

    @Override
    public void start(Stage stage) throws Exception {

        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();

        client = SimpleClient.getClient();// Adjust port if needed
        client.openConnection();

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

    @Override
    public void stop() throws Exception {
        super.stop();
        client.closeConnection();
    }

    public void updateCatalog(List<String> catalog) {
        catalogList.getItems().setAll(catalog);
    }

    public void updateItemDetails(String item) {
        itemDetails.setText("Item details:\n" + item.replace(",", "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Tester.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

}
