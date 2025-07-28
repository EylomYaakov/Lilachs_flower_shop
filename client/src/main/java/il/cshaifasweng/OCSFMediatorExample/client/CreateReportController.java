package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateReportController {

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<String> reportTypes;

    @FXML
    private ComboBox<String> shopsList;

    @FXML
    private DatePicker startDate;

    @FXML
    private Label shopLabel;

    @FXML
    public void initialize() {
        Utils.initReportOptions(startDate, endDate, reportTypes, shopsList, shopLabel);
    }

    @FXML
    void createReport(ActionEvent event) {
        String shop = shopsList.getSelectionModel().getSelectedItem();
        String type = reportTypes.getSelectionModel().getSelectedItem();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        if(shop == null || type == null || start == null || end == null){
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("report.fxml"));
            Parent root = loader.load();
            ReportController controller = loader.getController();
            controller.setEndDate(end);
            controller.setStartDate(start);
            controller.setShop(shop);
            controller.setReportType(type);
            Utils.sendReportMessage(type);
            Scene scene = new Scene(root);
            Stage stage = (Stage) startDate.getScene().getWindow();  // Or any valid stage
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

}
