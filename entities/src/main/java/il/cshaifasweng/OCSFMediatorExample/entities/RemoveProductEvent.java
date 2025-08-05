package il.cshaifasweng.OCSFMediatorExample.entities;


import java.io.Serializable;

public class RemoveProductEvent implements Serializable {
    private int productId;
    public RemoveProductEvent(int productId) {
        this.productId = productId;
    }
    public int getProductId() {
        return productId;
    }
}

