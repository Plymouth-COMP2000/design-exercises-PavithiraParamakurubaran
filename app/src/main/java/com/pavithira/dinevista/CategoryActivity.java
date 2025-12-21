package com.pavithira.dinevista;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CategoryActivity extends BaseActivity {

    private ImageView banner;
    private TextView categoryTitle;
    private LinearLayout foodListContainer;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        setupBottomNavigation(R.id.nav_menu);
        setupDrawer();

        banner = findViewById(R.id.bannerImage);
        categoryTitle = findViewById(R.id.categoryTitle);
        foodListContainer = findViewById(R.id.foodListContainer);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // ✅ GET INTEGER CATEGORY ID
        int categoryId = getIntent().getIntExtra("categoryId", -1);
        String title = getIntent().getStringExtra("categoryTitle");
        String imageName = getIntent().getStringExtra("categoryImage");

        if (categoryId == -1) {
            Toast.makeText(this, "Invalid category!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        categoryTitle.setText(title != null ? title : "Category");

        int bannerResId = getResources()
                .getIdentifier(imageName, "drawable", getPackageName());
        banner.setImageResource(
                bannerResId != 0 ? bannerResId : R.drawable.default_food
        );

        loadFoodsByCategory(categoryId);
    }

    private void loadFoodsByCategory(int categoryId) {
        foodListContainer.removeAllViews();

        Cursor cursor = db.rawQuery(
                "SELECT title, description, image, price FROM foods WHERE category_id = ?",
                new String[]{String.valueOf(categoryId)}
        );

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No foods found in this category", Toast.LENGTH_LONG).show();
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String desc = cursor.getString(1);
            String img = cursor.getString(2);
            double price = cursor.getDouble(3);

            addFoodItemToLayout(name, desc, img, price);
        }

        cursor.close();
    }

    private void addFoodItemToLayout(String name, String desc, String img, double price) {

        android.view.View foodItem =
                getLayoutInflater().inflate(R.layout.item_food, foodListContainer, false);

        ImageView imgFood = foodItem.findViewById(R.id.imgFood);
        TextView txtName = foodItem.findViewById(R.id.txtFoodName);
        TextView txtDesc = foodItem.findViewById(R.id.txtFoodDesc);
        TextView txtPrice = foodItem.findViewById(R.id.txtFoodPrice);
        LinearLayout btnAddToCart = foodItem.findViewById(R.id.btnAddToCart);

        txtName.setText(name);
        txtDesc.setText(desc);
        txtPrice.setText("RM " + price);

        int imgResId = getResources().getIdentifier(img, "drawable", getPackageName());
        imgFood.setImageResource(
                imgResId != 0 ? imgResId : R.drawable.default_food
        );

        btnAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addItem(name, desc, price);
            Toast.makeText(this, name + " added to cart", Toast.LENGTH_SHORT).show();
        });

        foodListContainer.addView(foodItem);
    }
}
