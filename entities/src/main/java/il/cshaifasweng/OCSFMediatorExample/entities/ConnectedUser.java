package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ConnectedUser implements Serializable{
    int id;
    String Username;
    String UserID;
    String creditId;
    String password;
    String role;
    String signUpDate;

    public ConnectedUser(int id, String username, String password, String UserId, String creditId, String role, String signUpDate) {
        this.id = id;
        this.Username=username;
        this.password=password;
        this.UserID=UserId;
        this.creditId=creditId;
        this.role=role;
        this.signUpDate=signUpDate;
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

    public void setUsername(String username) {
        Username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setRole(String role) {
        this.role = role;
    }

    public String getSignUpDate() {
        return signUpDate;
    }


    public int getId() {return id;}

    public String getShop(){
        if(role.startsWith("worker:manager:shop:")){
            return role.split(":", 4)[3];
        }
        else if(role.startsWith("shop account:")){
            return role.split(":", 2)[1];
        }
        return "all chain";
    }
}
