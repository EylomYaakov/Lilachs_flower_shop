package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
public class ChangeUserDetailsEvent implements Serializable {
    private final ConnectedUser user;
    private final String changed;

    public ChangeUserDetailsEvent(ConnectedUser user, String changed) {
        this.user = user;
        this.changed = changed;
    }

    public ConnectedUser getUser() {
        return user;
    }

    public String getChanged() {
        return changed;
    }
}
