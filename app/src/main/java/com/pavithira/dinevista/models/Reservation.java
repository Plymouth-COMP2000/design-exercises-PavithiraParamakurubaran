package com.pavithira.dinevista.models;

import java.util.List;

public class Reservation {
    public String name;
    public int id;
    private String userId;
    public String email;
    public String phone;
    public String pax;
    public String date;
    public String time;
    public String request;
    public double subtotal;
    public String status; // pending, upcoming, cancelled, completed
    public List<CartItem> items;

    public Reservation() { } // Default constructor required for Firebase

    public Reservation(String name, String email, String phone, String pax, String date, String time, String request,
                       double subtotal, String status, List<CartItem> items, String userId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pax = pax;
        this.date = date;
        this.time = time;
        this.request = request;
        this.subtotal = subtotal;
        this.status = status;
        this.items = items;
        this.userId = userId;
    }
}
