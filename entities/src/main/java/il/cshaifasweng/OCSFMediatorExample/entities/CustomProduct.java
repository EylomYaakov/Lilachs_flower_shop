package il.cshaifasweng.OCSFMediatorExample.entities;

public class CustomProduct extends BaseProduct {
    public String priceRange;
    public String color;

    public CustomProduct(String type, String priceRange, String color){
        super(type);
        this.priceRange = priceRange;
        this.color = color;
    }

    public double getAveragePrice(){
        int dashIndex = priceRange.indexOf("-");
        int minPrice = Integer.parseInt(priceRange.substring(0, dashIndex-1));
        int maxPrice = Integer.parseInt(priceRange.substring(dashIndex+1, priceRange.length()-1));
        return (minPrice + maxPrice)/2.0;
    }

    public boolean equals(CustomProduct product){
        return product.priceRange.equals(this.priceRange) && product.color.equals(this.color) && product.type.equals(this.type);
    }
}
