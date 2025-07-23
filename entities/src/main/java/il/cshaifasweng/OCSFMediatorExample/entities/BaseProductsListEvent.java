package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.util.List;

public class BaseProductsListEvent implements Serializable {
    private final List<BaseProduct> baseProducts;

    public BaseProductsListEvent(List<BaseProduct> baseProducts) {
        this.baseProducts = baseProducts;
    }

    public List<BaseProduct> getBaseProducts() {
        return baseProducts;
    }
}
