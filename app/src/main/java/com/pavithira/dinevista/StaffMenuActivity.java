package com.pavithira.dinevista;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.pavithira.dinevista.models.FoodCategory;
import com.pavithira.dinevista.models.FoodItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StaffMenuActivity extends StaffBaseActivity {

    private LinearLayout linearCategories, linearFoodList;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private int selectedCategoryId = -1; // -1 = all categories

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu);

        // ---------------- Setup Base ----------------
        setupBottomNavigation(R.id.nav_menu);
        setupDrawer();

        linearCategories = findViewById(R.id.linearCategories);
        linearFoodList = findViewById(R.id.linearFoodList);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        loadCategoriesFromDatabaseAsync();
        loadFoodListFromDatabaseAsync();

        // Add FAB listener
        findViewById(R.id.fabAddFood).setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAddNewItemActivity.class);
            startActivity(intent);
        });
    }

    private void loadCategoriesFromDatabaseAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Cursor cursor = db.rawQuery("SELECT id, title, image FROM categories", null);
            List<FoodCategory> categoryList = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String imgName = cursor.getString(2);

                    int imgResId = getResources().getIdentifier(imgName, "drawable", getPackageName());
                    if (imgResId == 0) imgResId = R.drawable.default_food;

                    categoryList.add(new FoodCategory(title, imgResId, null)); // activity not needed
                } while (cursor.moveToNext());
            }
            cursor.close();

            runOnUiThread(() -> {
                linearCategories.removeAllViews();
                for (FoodCategory cat : categoryList) {
                    addCategoryToLayout(cat);
                }
            });
        });
    }

    private void addCategoryToLayout(FoodCategory cat) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_category_staff, linearCategories, false);
        ImageView img = view.findViewById(R.id.imgCategoryStaff);
        TextView txt = view.findViewById(R.id.txtCategoryStaff);

        img.setImageResource(cat.getImageResId());
        txt.setText(cat.getTitle());

        view.setOnClickListener(v -> {
            // Filter food list by this category
            selectedCategoryId = getCategoryIdByTitle(cat.getTitle());
            loadFoodListFromDatabaseAsync();
        });

        linearCategories.addView(view);
    }

    private int getCategoryIdByTitle(String title) {
        Cursor cursor = db.rawQuery("SELECT id FROM categories WHERE title = ?", new String[]{title});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    private void loadFoodListFromDatabaseAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String query = "SELECT id, category_id, title, description, image, price FROM foods";
            String[] args = null;
            if (selectedCategoryId != -1) {
                query += " WHERE category_id = ?";
                args = new String[]{String.valueOf(selectedCategoryId)};
            }

            Cursor cursor = db.rawQuery(query, args);
            List<FoodItem> foodList = new ArrayList<>();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int catId = cursor.getInt(1);
                String title = cursor.getString(2);
                String desc = cursor.getString(3);
                String imgName = cursor.getString(4);
                double price = cursor.getDouble(5);

                int imgResId = getResources().getIdentifier(imgName, "drawable", getPackageName());
                if (imgResId == 0) imgResId = R.drawable.default_food;

                foodList.add(new FoodItem(id, catId, title, desc, imgResId, price));
            }
            cursor.close();

            runOnUiThread(() -> {
                linearFoodList.removeAllViews();
                for (FoodItem item : foodList) {
                    addFoodItemToLayout(item);
                }
            });
        });
    }

    private void addFoodItemToLayout(FoodItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_food_staff, linearFoodList, false);

        ImageView imgFood = view.findViewById(R.id.imgFoodStaff);
        TextView txtName = view.findViewById(R.id.txtFoodNameStaff);
        TextView txtDesc = view.findViewById(R.id.txtFoodDescStaff);
        TextView txtPrice = view.findViewById(R.id.txtFoodPriceStaff);
        LinearLayout btnEdit = view.findViewById(R.id.btnEditFoodStaff);
        LinearLayout btnRemove = view.findViewById(R.id.btnRemoveFoodStaff);

        txtName.setText(item.getTitle());
        txtDesc.setText(item.getDescription());
        txtPrice.setText("RM " + item.getPrice());
        imgFood.setImageResource(item.getImageResId());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffEditItemActivity.class);
            intent.putExtra("foodId", item.getId());
            startActivity(intent);
        });

        btnRemove.setOnClickListener(v -> {
            db.delete("foods", "id = ?", new String[]{String.valueOf(item.getId())});
            Toast.makeText(this, item.getTitle() + " removed", Toast.LENGTH_SHORT).show();
            loadFoodListFromDatabaseAsync(); // refresh
        });

        linearFoodList.addView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}

