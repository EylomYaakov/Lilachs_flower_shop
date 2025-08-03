package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SignUpEvent implements Serializable {
    String attemptStatus;
    ConnectedUser connectedUser;
    int id;

    public SignUpEvent(String attemptStatus, ConnectedUser connectedUser, int id) {
        this.attemptStatus = attemptStatus;
        this.connectedUser = connectedUser;
        this.id = id;

    }

    public SignUpEvent(String attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public String getStatus() {
        return attemptStatus;
    }


    public ConnectedUser getConnectedUser() {   return connectedUser; }

    public int getId() {return id; }
}
