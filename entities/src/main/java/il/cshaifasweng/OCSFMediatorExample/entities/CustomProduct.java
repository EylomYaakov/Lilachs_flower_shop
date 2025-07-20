package il.cshaifasweng.OCSFMediatorExample.entities;

public class CustomProduct extends BaseProduct {
    public String priceRange;
    public String color;

    public CustomProduct(String type, String priceRange, String color){
        super(type);
        this.priceRange = priceRange;
        this.color = color;
    }
}
