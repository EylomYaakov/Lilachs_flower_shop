package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class UsersListEvent implements Serializable {
    private final List<ConnectedUser> users;

    public UsersListEvent(List<ConnectedUser> users) {
        this.users = users;
    }

    public List<ConnectedUser> getUsers() {
        return users;
    }
}
