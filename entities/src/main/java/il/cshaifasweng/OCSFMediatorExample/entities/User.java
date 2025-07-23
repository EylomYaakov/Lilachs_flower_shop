package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String password;
    private String id;
    private String creditCard;
    private String role;
    private String accountType;
    private int databaseId;

    public User(String name, String password, String id, String creditCard, String role, String accountType) {
        this.name = name;
        this.password = password;
        this.id = id;
        this.creditCard = creditCard;
        this.role = role;
        this.accountType = accountType;
        databaseId = -1;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getRole() {
        return role;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
