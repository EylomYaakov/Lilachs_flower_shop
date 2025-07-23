package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class Order implements Serializable {
    private Map<BaseProduct, Integer> products;
    private String greetingCard;
    private String address;
    private String phoneNumber;
    private String name;
    private LocalDateTime deliveryTime;
    private LocalDate orderDate;
    private int customerId;
    private boolean cancelled;
    private double refund;
    private int id;


    public Order(Map<BaseProduct, Integer> products, String greetingCard, String address, String phoneNumber, String name, LocalDateTime deliveryTime, LocalDate orderDate, int customerId) {
        this.products = products;
        this.greetingCard = greetingCard;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.deliveryTime = deliveryTime;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.cancelled = false;
        this.refund = 0;
        this.id = -1;
    }

    public Map<BaseProduct, Integer> getProducts() {
        return products;
    }

    public String getGreetingCard() {
        return greetingCard;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public int getCustomerId() {
        return customerId;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public double getRefund() {
        return refund;
    }

    public void setRefund(double refund) {
        this.refund = refund;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

}
