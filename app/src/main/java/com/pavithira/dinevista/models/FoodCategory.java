package com.pavithira.dinevista.models;

public class FoodCategory {
    private String title;
    private int imageResId; // drawable resource
    private Class<?> activityClass; // Activity to open when clicked

    public FoodCategory(String title, int imageResId, Class<?> activityClass) {
        this.title = title;
        this.imageResId = imageResId;
        this.activityClass = activityClass;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}
