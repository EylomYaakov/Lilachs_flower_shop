package il.cshaifasweng.OCSFMediatorExample.entities;


import java.io.Serializable;

public class InitDescriptionEvent implements Serializable {
    private final Product product;

    public InitDescriptionEvent(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

}
