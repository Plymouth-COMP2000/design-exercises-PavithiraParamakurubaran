package com.pavithira.dinevista;

import com.pavithira.dinevista.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    // ✅ UPDATED addItem method
    public void addItem(String name, String description, double price) {
        for (CartItem item : cartItems) {
            if (item.getName().equals(name)) {
                item.increaseQty();
                return;
            }
        }
        cartItems.add(new CartItem(name, description, price, 1));
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
