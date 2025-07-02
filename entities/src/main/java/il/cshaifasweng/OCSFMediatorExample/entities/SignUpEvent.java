package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class SignUpEvent implements Serializable {
    String attemptStatus;

    public SignUpEvent(String attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public String getStatus() {
        return attemptStatus;
    }
}
