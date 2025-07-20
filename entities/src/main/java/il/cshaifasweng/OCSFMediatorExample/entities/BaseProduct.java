package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public abstract class BaseProduct implements Serializable {
    public String type;

    public BaseProduct(String type) {
        this.type = type;
    }
}
