package il.cshaifasweng.OCSFMediatorExample.entities;


public class Product extends BaseProduct{

    public int id;
    public String name;
    public String description;
    public double price;
    public byte[] image;
    public int sale;
    public String shop;


    public Product(int id, String name, String type, String description, double price, byte[] image) {
        super(type);
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.sale = 0;
        this.shop = "all chain";
    }
}
