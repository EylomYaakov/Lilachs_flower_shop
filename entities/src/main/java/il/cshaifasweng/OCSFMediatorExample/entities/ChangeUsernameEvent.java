package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
public class ChangeUsernameEvent implements Serializable {
    private final String status;

    public ChangeUsernameEvent(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
