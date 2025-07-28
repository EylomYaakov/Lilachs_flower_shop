package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.ComplaintsListEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplaintsController {

    @FXML
    private CheckBox acceptCheckBox1;

    @FXML
    private CheckBox acceptCheckBox2;

    @FXML
    private CheckBox acceptCheckBox3;

    @FXML
    private Label acceptedLabel1;

    @FXML
    private Label acceptedLabel2;

    @FXML
    private Label acceptedLabel3;

    @FXML
    private Label complaintsEmpty;

    @FXML
    private TextField complaintText1;

    @FXML
    private TextField complaintText2;

    @FXML
    private TextField complaintText3;

    @FXML
    private Label title;

    @FXML
    private Label complaintLabel1;

    @FXML
    private Label complaintLabel2;

    @FXML
    private Label complaintLabel3;

    @FXML
    private Button leftArrow;

    @FXML
    private TextField response1;

    @FXML
    private TextField response2;

    @FXML
    private TextField response3;

    @FXML
    private Label responseLabel1;

    @FXML
    private Label responseLabel2;

    @FXML
    private Label responseLabel3;

    @FXML
    private Label statusLabel1;

    @FXML
    private Label statusLabel2;

    @FXML
    private Label statusLabel3;

    @FXML
    private Button sendResponseButton1;

    @FXML
    private Button sendResponseButton2;

    @FXML
    private Button sendResponseButton3;

    @FXML
    private Button rightArrow;


    private Label[] complaintLabels;
    private Label[] responseLabels;
    private TextField[] complaintTexts;
    private TextField[] responses;
    private Button[] responseButtons;
    private Label[] statusLabels;
    private CheckBox[] acceptCheckBoxes;
    private Label[] acceptedLabels;
    private final int pageSize = 3;
    private List<Complaint> complaints;
    private Paginator<Complaint> paginator;


    @FXML
    public void initialize(){
        complaintLabels = new Label[]{complaintLabel1, complaintLabel2, complaintLabel3};
        responseLabels = new Label[]{responseLabel1, responseLabel2, responseLabel3};
        complaintTexts = new TextField[]{complaintText1, complaintText2, complaintText3};
        responses = new TextField[]{response1, response2, response3};
        responseButtons = new Button[]{sendResponseButton1, sendResponseButton2, sendResponseButton3};
        statusLabels = new Label[]{statusLabel1, statusLabel2, statusLabel3};
        acceptCheckBoxes = new CheckBox[]{acceptCheckBox1, acceptCheckBox2, acceptCheckBox3};
        acceptedLabels = new Label[]{acceptedLabel1, acceptedLabel2, acceptedLabel3};
        EventBus.getDefault().register(this);
        try{
            if(SimpleClient.getRole().startsWith("worker")){
                SimpleClient.getClient().sendToServer("GET_COMPLAINTS");
            }
            else {
                SimpleClient.getClient().sendToServer("GET_COMPLAINTS:" + SimpleClient.getId());
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //only for complaints to be not empty
        complaints = new ArrayList<>();
        LocalDate date = LocalDate.now();
        Complaint complaint1 = new Complaint("A", "Haifa", 1, 1, date);
        Complaint complaint2 = new Complaint("B","Haifa", 1, 1, date);
        Complaint complaint3 = new Complaint("C", "Tel Aviv",1, 1, date);
        Complaint complaint4 = new Complaint("D", "Tel Aviv",1, 1, date);
        Complaint complaint5 = new Complaint("E", "Jerusalem",1, 1, date);
        Complaint complaint6 = new Complaint("F", "Jerusalem",1, 1, date);
        complaint3.setResponse("OK");
        complaints.add(complaint1);
        complaints.add(complaint2);
        complaints.add(complaint3);
        complaints.add(complaint4);
        complaints.add(complaint5);
        complaints.add(complaint6);
        Collections.reverse(complaints);
        ComplaintsListEvent event = new ComplaintsListEvent(complaints);
        initComplaints(event);
    }

    @Subscribe
    public void initComplaints(ComplaintsListEvent event){
        List<Complaint> complaints = event.getComplaints();
        Collections.reverse(complaints);
        String shop = SimpleClient.getUser().getShop();
        if(SimpleClient.getRole().startsWith("worker") && !shop.equals("all chain")){
            this.complaints = filterComplaints(complaints, shop);
        }
        else{
            this.complaints = complaints;
        }
        paginator = new Paginator<>(this.complaints, pageSize);
        renderPage();
    }

    @FXML
    void toMenu(ActionEvent event) {
        App.switchScreen("menu");
    }

    @FXML
    void leftArrowPressed(ActionEvent event) {
        paginator.prevPage();
        renderPage();
    }

    @FXML
    void rightArrowPressed(ActionEvent event) {
        renderPage();
    }

    private void renderPage(){
        List<Complaint> pageItems = paginator.getCurrentPageItems();
        if(pageItems.isEmpty()){
            Platform.runLater(()->complaintsEmpty.setVisible(true));
        }
        else{
            Platform.runLater(()->complaintsEmpty.setVisible(false));
        }
        String accountType = SimpleClient.getRole();
        if(accountType.startsWith("worker")){
           Platform.runLater(() -> title.setText("complaints"));
        }
        for(int i=0; i<complaintLabels.length; i++){
            if(i<pageItems.size()){
                int finalI = i;
                setComplaintVisibility(i,true);
                Complaint complaint = complaints.get(paginator.getCurrentIndex() - paginator.getCurrentPageSize() + i);
                Platform.runLater(() -> complaintTexts[finalI].setText(complaint.getComplaint()));
                if(accountType.startsWith("worker")){
                    renderResponseWorker(i, complaint);
                }
                else{
                    renderResponseCustomer(i, complaint);
                }
            }
            else{
                setComplaintVisibility(i,false);
            }
        }
        Platform.runLater(()->rightArrow.setVisible(paginator.hasNextPage()));
        Platform.runLater(()->leftArrow.setVisible(paginator.hasPreviousPage()));
    }

    private void setComplaintVisibility(int i, boolean visible){
        Platform.runLater(()->complaintLabels[i].setVisible(visible));
        Platform.runLater(()->responseLabels[i].setVisible(visible));
        Platform.runLater(()->complaintTexts[i].setVisible(visible));
        Platform.runLater(()->responses[i].setVisible(visible));
        Platform.runLater(()->responseButtons[i].setVisible(false));
        Platform.runLater(()->acceptCheckBoxes[i].setVisible(false));
        Platform.runLater(()->responses[i].setEditable(false));
        Platform.runLater(()->acceptedLabels[i].setVisible(false));
        Platform.runLater(()->statusLabels[i].setVisible(false));
    }


    private void renderResponseCustomer(int index, Complaint complaint){
        if(complaint.getResponse().isEmpty()){
            Platform.runLater(()->responses[index].setVisible(false));
            Platform.runLater(()->responseLabels[index].setText("No response yet"));
        }
        else{
            Platform.runLater(()->responseLabels[index].setText("response:"));
            showAnswer(index, complaint);
        }
    }

    private void renderResponseWorker(int index, Complaint complaint){
        if(complaint.getResponse().isEmpty()){
            Platform.runLater(()->responses[index].setEditable(true));
            Platform.runLater(()->responseButtons[index].setVisible(true));
            Platform.runLater(()->acceptCheckBoxes[index].setVisible(true));
        }
        showAnswer(index, complaint);
    }

    private void showAnswer(int index, Complaint complaint){
        Platform.runLater(()->responses[index].setText(complaint.getResponse()));
        if(!complaint.getResponse().isEmpty()) {
            Platform.runLater(() -> acceptedLabels[index].setVisible(true));
            if (complaint.getAccepted()) {
                Platform.runLater(() -> acceptedLabels[index].setText("complaint accepted!"));
            } else {
                Platform.runLater(() -> acceptedLabels[index].setText("complaint rejected"));
            }
        }
    }

    @FXML
    void sendResponse(ActionEvent event) {
        Button button = (Button) event.getSource();
        for(int i=0; i<responseButtons.length; i++){
            if(button == responseButtons[i]){
                int finalI = i;
                String response = responses[i].getText();
                if(response.isEmpty()){
                    return;
                }
                Platform.runLater(()->responseButtons[finalI].setVisible(false));
                Platform.runLater(()->statusLabels[finalI].setVisible(true));
                Platform.runLater(()->acceptedLabels[finalI].setVisible(true));
                Platform.runLater(()->acceptCheckBoxes[finalI].setVisible(false));
                Complaint complaint = complaints.get(paginator.getCurrentIndex() - paginator.getCurrentPageSize()+ i);
                complaint.setResponse(response);
                if(acceptCheckBoxes[finalI].isSelected()){
                    complaint.setAccepted(true);
                    Platform.runLater(()->acceptedLabels[finalI].setText("complaint accepted!"));
                }
                else{
                    Platform.runLater(()->acceptedLabels[finalI].setText("complaint rejected"));
                }
                try{
                    SimpleClient.getClient().sendToServer(complaint);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    private List<Complaint> filterComplaints(List<Complaint> complaints, String shop){
        List<Complaint> filteredComplaints = new ArrayList<>();
        for (Complaint complaint : complaints) {
            if (complaint.getShop().equals(shop)) {
                filteredComplaints.add(complaint);
            }
        }
        return filteredComplaints;
    }


}
