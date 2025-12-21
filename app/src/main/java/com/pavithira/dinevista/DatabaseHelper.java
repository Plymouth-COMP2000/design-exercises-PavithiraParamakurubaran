package com.pavithira.dinevista;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "dinevista.db";
    private static final int DB_VERSION = 5; // Increment DB version

    // Notifications table
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ---------------- CATEGORIES ----------------
        db.execSQL(
                "CREATE TABLE categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "image TEXT)"
        );

        // ---------------- FOODS ----------------
        db.execSQL(
                "CREATE TABLE foods (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "description TEXT," +
                        "image TEXT," +
                        "price REAL," +
                        "category_id INTEGER," +
                        "FOREIGN KEY(category_id) REFERENCES categories(id))"
        );

        // ---------------- USERS ----------------
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "email TEXT," +
                        "phone TEXT)"
        );

        // ---------------- RESERVATIONS ----------------
        db.execSQL(
                "CREATE TABLE reservations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "custom_id TEXT," +
                        "name TEXT," +
                        "email TEXT," +
                        "phone TEXT," +
                        "pax TEXT," +
                        "date TEXT," +
                        "time TEXT," +
                        "request TEXT," +
                        "subtotal REAL," +
                        "cart_items TEXT," +
                        "status TEXT" +
                        ")"
        );

        // ---------------- NOTIFICATIONS ----------------
        db.execSQL(
                "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "custom_id TEXT," +
                        "title TEXT," +
                        "message TEXT," +
                        "timestamp INTEGER," +
                        "read INTEGER DEFAULT 0" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS reservations");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS foods");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS); // Drop notifications table
        onCreate(db);
    }
}
