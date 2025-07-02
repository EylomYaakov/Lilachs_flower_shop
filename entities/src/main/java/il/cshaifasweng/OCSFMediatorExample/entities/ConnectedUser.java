package il.cshaifasweng.OCSFMediatorExample.entities;

public class ConnectedUser {
    String Username;
    int UserID;
    int creditId;
    String userType;

    public ConnectedUser(String username,int UserId,int creditId,String userType) {
        this.Username=username;
        this.UserID=UserId;
        this.creditId=creditId;
        this.userType=userType;
    }


    public String getUsername() {
        return Username;
    }
}
