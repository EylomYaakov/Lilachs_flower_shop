package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class LogoutEvent implements Serializable {
    private final String status; // "LOGOUT_OK" / "LOGOUT_NOT_FOUND"
    public LogoutEvent(String status){ this.status = status; }
    public String getStatus(){ return status; }
}
