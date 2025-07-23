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
    public static Stage appStage;

    @Override
    public void start(Stage stage) throws IOException {
        appStage = stage;
        client = SimpleClient.getClient();
        client.openConnection();
        scene = new Scene(loadFXML("catalog"));
        stage.setScene(scene);
        stage.show();

        System.out.println(" Connected to server");
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
        EventBus.getDefault().unregister(this);
        SimpleClient.getClient().sendToServer("remove client");
        SimpleClient.getClient().closeConnection();
        super.stop();
    }

    public static void setWindowTitle(String title) {
        appStage.setTitle(title);
    }

    public static void setContent(String pageName) throws IOException {
        Parent root = loadFXML(pageName);
        scene = new Scene(root);
        appStage.setScene(scene);
        appStage.show();
    }

    public static void switchScreen (String screenName) {
        Platform.runLater(() -> {
            setWindowTitle(screenName);
            try {
                setContent(screenName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }

}