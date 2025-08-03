package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class LoginEvent implements Serializable {
    String attemptStatus;
    ConnectedUser connectedUser;
    int id;

    public LoginEvent(String attemptStatus, ConnectedUser connectedUser, int id) {
        this.attemptStatus = attemptStatus;
        this.connectedUser = connectedUser;
        this.id = id;
    }

    public LoginEvent(String attemptStatus) {
        this.attemptStatus = attemptStatus;
        this.connectedUser = null;
        this.id = -1;
    }

    public String getStatus() {
        return attemptStatus;
    }


    public ConnectedUser getConnectedUser() {return connectedUser;}

    public int getId() {return id;}
}



