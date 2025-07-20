package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Sale implements Serializable {
    private List<Product> products;
    private int saleAmount;
    LocalDateTime end;

    public Sale(List<Product> products, int saleAmount, LocalDateTime end) {
        this.products = products;
        this.saleAmount = saleAmount;
        this.end = end;
    }

    public List<Product> getProducts() {
        return products;
    }

    public int getSaleAmount() {
        return saleAmount;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
