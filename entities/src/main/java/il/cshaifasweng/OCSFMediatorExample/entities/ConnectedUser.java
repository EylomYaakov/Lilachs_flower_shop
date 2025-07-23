package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ConnectedUser implements Serializable{
    String Username;
    String UserID;
    String creditId;
    String userType;
    String password;
    String role;

    public ConnectedUser(String username, String password, String UserId, String creditId, String role, String userType) {
        this.Username=username;
        this.password=password;
        this.UserID=UserId;
        this.creditId=creditId;
        this.role=role;
        this.userType=userType;
    }


    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserID() {
        return UserID;
    }

    public String getCreditCard() {
        return creditId;
    }

    public String getRole() {
        return role;
    }

    public String getAccountType() {
        return userType;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccountType(String accountType) {
        this.userType = accountType;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
