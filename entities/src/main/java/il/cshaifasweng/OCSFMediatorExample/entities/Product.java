package il.cshaifasweng.OCSFMediatorExample.entities;


public class Product extends BaseProduct{

    public int id;
    public String name;
    public String description;
    public double price;
    public byte[] image;
    public int sale;
    public String shop;


    public Product(int id, String name, String type, String description, double price, byte[] image, String shop) {
        super(type);
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.sale = 0;
        this.shop = shop;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public byte[] getImage() {
        return image;
    }

    public String getShop() {
        return shop;
    }

    public void setId(int newProductId) {
        this.id = newProductId;
    }

    public void setPrice(double price) {
    }
    public void setSale(int sale)
    {
        this.sale = sale;
    }

    public int getSale()
    {
        return sale;
    }
}
