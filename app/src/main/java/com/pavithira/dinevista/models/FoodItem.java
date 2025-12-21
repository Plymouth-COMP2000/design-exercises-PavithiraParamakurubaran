package com.pavithira.dinevista.models;

public class FoodItem {
    private int id;
    private int categoryId;
    private String title;
    private String description;
    private int imageResId;
    private double price;

    public FoodItem(int id, int categoryId, String title, String description, int imageResId, double price) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.price = price;
    }

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public double getPrice() { return price; }
}
