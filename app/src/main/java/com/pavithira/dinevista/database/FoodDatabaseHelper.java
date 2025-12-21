package com.pavithira.dinevista.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pavithira.dinevista.models.FoodCategory;
import com.pavithira.dinevista.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class FoodDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dinevista.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_CATEGORY = "categories";
    private static final String TABLE_FOOD = "foods";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_IMAGE = "image"; // drawable resource name
    private static final String COL_CATEGORY_ID = "category_id";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PRICE = "price";

    public FoodDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT, "
                + COL_IMAGE + " TEXT)";
        db.execSQL(createCategoryTable);

        String createFoodTable = "CREATE TABLE " + TABLE_FOOD + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CATEGORY_ID + " INTEGER, "
                + COL_TITLE + " TEXT, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_IMAGE + " TEXT, "
                + COL_PRICE + " REAL, "
                + "FOREIGN KEY(" + COL_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COL_ID + "))";
        db.execSQL(createFoodTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    // Insert Category
    public long insertCategory(FoodCategory category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, category.getTitle());
        values.put(COL_IMAGE, String.valueOf(category.getImageResId()));
        return db.insert(TABLE_CATEGORY, null, values);
    }

    // Insert Food Item
    public long insertFood(FoodItem food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORY_ID, food.getCategoryId());
        values.put(COL_TITLE, food.getTitle());
        values.put(COL_DESCRIPTION, food.getDescription());
        values.put(COL_IMAGE, String.valueOf(food.getImageResId()));
        values.put(COL_PRICE, food.getPrice());
        return db.insert(TABLE_FOOD, null, values);
    }

    // Get all categories
    public List<FoodCategory> getAllCategories() {
        List<FoodCategory> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                int imageRes = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
                categories.add(new FoodCategory(title, imageRes, null));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    // Get foods by category
    public List<FoodItem> getFoodsByCategory(int categoryId) {
        List<FoodItem> foods = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOOD + " WHERE " + COL_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                int imageRes = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));
                foods.add(new FoodItem(id, categoryId, title, description, imageRes, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foods;
    }

    public int getCategoryIdByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        int id = -1;
        Cursor cursor = db.rawQuery("SELECT id FROM categories WHERE title=?", new String[]{title});
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }
    // Search function
    public List<FoodItem> searchFood(String query) {
        List<FoodItem> foods = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM foods WHERE title LIKE ? OR description LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"}
        );

        while (cursor.moveToNext()) {
            foods.add(new FoodItem(
                    cursor.getInt(cursor.getColumnIndex("id")),                // id
                    cursor.getInt(cursor.getColumnIndex("category_id")),       // categoryId
                    cursor.getString(cursor.getColumnIndex("title")),          // title
                    cursor.getString(cursor.getColumnIndex("description")),    // description
                    cursor.getInt(cursor.getColumnIndex("image")),             // imageResId
                    cursor.getDouble(cursor.getColumnIndex("price"))           // price
            ));
        }

        cursor.close();
        return foods;
    }

}
