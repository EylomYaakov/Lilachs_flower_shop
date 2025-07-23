package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
public class ChangeUserDetailsEvent implements Serializable {
    private final String status;

    public ChangeUserDetailsEvent(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
