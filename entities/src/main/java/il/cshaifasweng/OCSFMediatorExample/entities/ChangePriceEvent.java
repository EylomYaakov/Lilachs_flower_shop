package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class ChangePriceEvent implements Serializable {
    private final Product product;

    public ChangePriceEvent(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

}
