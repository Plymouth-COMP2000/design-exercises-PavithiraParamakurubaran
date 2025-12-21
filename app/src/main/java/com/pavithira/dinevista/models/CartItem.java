package com.pavithira.dinevista.models;

import java.io.Serializable;

public class CartItem implements Serializable {  // <-- implement Serializable
    private String name;
    private String description;
    private double price;
    private int qty;

    public CartItem() { }

    public CartItem(String name, String description, double price, int qty) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.qty = qty;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQty() { return qty; }

    public void increaseQty() { qty++; }
    public void decreaseQty() { if (qty > 1) qty--; }
    public double getTotalPrice() { return price * qty; }
}
