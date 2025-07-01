package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class LoginEvent implements Serializable {
    String attemptStatus;

    public LoginEvent(String attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public String getStatus() {
        return attemptStatus;
    }
}

