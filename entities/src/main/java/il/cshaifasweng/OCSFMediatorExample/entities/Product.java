package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class Product implements Serializable {

    public int id;
    public String name;
    public String type;
    public String description;
    public double price;
    public byte[] image;
    public int sale;


    public Product(int id, String name, String type, String description, double price, byte[] image) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.image = image;
        this.sale = 0;

    }
}
