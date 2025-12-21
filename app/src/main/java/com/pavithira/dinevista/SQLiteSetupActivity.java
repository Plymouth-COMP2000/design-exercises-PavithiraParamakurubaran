package com.pavithira.dinevista;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SQLiteSetupActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        addCategoriesAndFoods();
        Toast.makeText(this, "SQLite data inserted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void addCategoriesAndFoods() {

        String[][] categories = {
                {"Snacks", "cat_snacks"},
                {"Soups", "cat_soups"},
                {"Main Course", "cat_main_course"},
                {"Rice & Noodles", "cat_rice_noodles"},
                {"Salads", "cat_salads"},
                {"Desserts", "cat_desserts"},
                {"Seafood", "cat_seafood"},
                {"Beverages", "cat_beverages"},
                {"Vegan", "cat_vegan"}
        };

        for (String[] cat : categories) {

            ContentValues catValues = new ContentValues();
            catValues.put("title", cat[0]);
            catValues.put("image", cat[1]);

            long categoryId = db.insert("categories", null, catValues);

            addFoodsForCategory((int) categoryId, cat[0]);
        }
    }

    private void addFoodsForCategory(int categoryId, String categoryTitle) {

        String[][] foods;

        switch (categoryTitle) {
            case "Snacks":
                foods = new String[][]{
                        {"French Fries", "Crispy golden fries", "food_fries", "7.5"},
                        {"Nachos", "Cheesy nachos with salsa", "food_nachos", "8.0"},
                        {"Spring Rolls", "Vegetable spring rolls", "food_spring_rolls", "6.5"}
                };
                break;
            case "Soups":
                foods = new String[][]{
                        {"Tomato Soup", "Fresh tomato soup", "food_tomato_soup", "5.0"},
                        {"Chicken Soup", "Chicken and vegetable soup", "food_chicken_soup", "6.5"},
                        {"Mushroom Soup", "Creamy mushroom soup", "food_mushroom_soup", "6.0"}
                };
                break;
            case "Main Course":
                foods = new String[][]{
                        {"Grilled Chicken", "Juicy grilled chicken", "food_grilled_chicken", "12.0"},
                        {"Beef Steak", "Tender beef steak", "food_beef_steak", "15.0"},
                        {"Paneer Butter Masala", "Spicy paneer dish", "food_paneer", "10.0"}
                };
                break;
            case "Rice & Noodles":
                foods = new String[][]{
                        {"Fried Rice", "Vegetable fried rice", "food_fried_rice", "8.0"},
                        {"Noodles", "Stir-fried noodles", "food_noodles", "7.5"},
                        {"Biryani", "Spicy chicken biryani", "food_biryani", "12.0"}
                };
                break;
            case "Salads":
                foods = new String[][]{
                        {"Caesar Salad", "Classic Caesar salad", "food_caesar", "6.0"},
                        {"Greek Salad", "Fresh Greek salad", "food_greek", "6.5"},
                        {"Fruit Salad", "Mixed seasonal fruits", "food_fruit", "5.5"}
                };
                break;
            case "Desserts":
                foods = new String[][]{
                        {"Chocolate Cake", "Rich chocolate cake", "food_chocolate_cake", "7.0"},
                        {"Ice Cream", "Vanilla ice cream scoop", "food_ice_cream", "5.0"},
                        {"Brownie", "Chocolate brownie", "food_brownie", "6.0"}
                };
                break;
            case "Seafood":
                foods = new String[][]{
                        {"Grilled Salmon", "Fresh grilled salmon", "food_salmon", "14.0"},
                        {"Fish & Chips", "Crispy fried fish", "food_fish_chips", "12.0"},
                        {"Prawn Curry", "Spicy prawn curry", "food_prawn_curry", "13.0"}
                };
                break;
            case "Beverages":
                foods = new String[][]{
                        {"Coca Cola", "Chilled Coke", "food_coke", "3.0"},
                        {"Orange Juice", "Freshly squeezed juice", "food_orange_juice", "4.0"},
                        {"Coffee", "Hot brewed coffee", "food_coffee", "4.5"}
                };
                break;
            case "Vegan":
                foods = new String[][]{
                        {"Vegan Burger", "Plant-based burger", "food_vegan_burger", "9.0"},
                        {"Tofu Stir Fry", "Tofu with vegetables", "food_tofu", "8.5"},
                        {"Vegan Salad", "Fresh vegan salad", "food_vegan_salad", "7.0"}
                };
                break;

            default:
                foods = new String[0][0];
        }

        for (String[] f : foods) {

            ContentValues foodValues = new ContentValues();
            foodValues.put("title", f[0]);
            foodValues.put("description", f[1]);
            foodValues.put("image", f[2]);
            foodValues.put("price", Double.parseDouble(f[3]));
            foodValues.put("category_id", categoryId);

            db.insert("foods", null, foodValues);
        }
    }
}
