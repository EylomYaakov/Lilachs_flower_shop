package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDate;

public class Complaint implements Serializable {
    private String complaint;
    private String shop;
    private int orderId;
    private int customerId;
    private int complaintId;
    private String response;
    private LocalDate date;
    private boolean accepted;

    public Complaint(String complaint, String shop, int orderId, int customerId, LocalDate date){
        this.complaint = complaint;
        this.shop = shop;
        this.orderId = orderId;
        this.customerId = customerId;
        this.complaintId = -1;
        this.response = "";
        this.date = date;
        this.accepted = false;
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

    public LocalDate getDate(){
        return date;
    }

    public boolean getAccepted(){
        return accepted;
    }

    public void setAccepted(boolean accepted){
        this.accepted = accepted;
    }

    public String getShop(){
        return shop;
    }
}

