package il.cshaifasweng.OCSFMediatorExample.server.ocsf;

public class SubscribedClient {
    private String username;
    private ConnectionToClient client;

    public SubscribedClient(ConnectionToClient client) {
        this.client = client;
        this.username="~";
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ConnectionToClient getClient() {
        return client;
    }

    public void setClient(ConnectionToClient client) {
        this.client = client;
    }
}