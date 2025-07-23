package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.time.LocalDate;
import java.util.List;

public class CompareReportsController {

    @FXML
    private BarChart<String, Number> histogram1;

    @FXML
    private BarChart<String, Number> histogram2;


    public void setHistogram1(BarChart<String, Number> source) {
        Platform.runLater(() -> histogram1.setTitle(source.getTitle()));
        for (XYChart.Series<String, Number> series : source.getData()) {
            XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
            newSeries.setName(series.getName());

            for (XYChart.Data<String, Number> data : series.getData()) {
                newSeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
            }
            Platform.runLater(() -> histogram1.getData().add(newSeries));
            Platform.runLater(() -> histogram1.setLegendVisible(source.isLegendVisible()));
        }
    }

    public BarChart<String, Number> getHistogram2() {
        return histogram2;
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }


}
