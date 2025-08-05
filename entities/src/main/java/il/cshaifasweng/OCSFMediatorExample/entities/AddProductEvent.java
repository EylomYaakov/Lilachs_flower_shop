package il.cshaifasweng.OCSFMediatorExample.entities;


import java.io.Serializable;

public class AddProductEvent implements Serializable {
    private Product product;
    public AddProductEvent(Product product) {
        this.product = product;
    }
    public Product getProduct() {
        return product;
    }
}
