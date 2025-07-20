package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class Complaint implements Serializable {
    private String complaint;
    private int orderId;
    private int customerId;
    private int complaintId;
    private String response;

    public Complaint(String complaint, int orderId, int customerId){
        this.complaint = complaint;
        this.orderId = orderId;
        this.customerId = customerId;
        this.complaintId = -1;
        this.response = "";
    }

    public int getOrderId(){
        return orderId;
    }

    public String getComplaint(){
        return complaint;
    }

    public int getCustomerId(){
        return customerId;
    }

    public int getComplaintId(){
        return complaintId;
    }

    public String getResponse(){
        return response;
    }

    public void setResponse(String response){
        this.response = response;
    }
}
