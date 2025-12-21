package com.pavithira.dinevista;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu, btnNotification;
    private EditText etSearch;
    private GridLayout gridCategories;
    private ImageView ivPopularBanner;
    private TextView tvUsername;

    private Handler bannerHandler = new Handler();
    private int[] bannerImages = {
            R.drawable.popularbanner1,
            R.drawable.popularbanner2,
            R.drawable.popularbanner3
    };
    private int currentBannerIndex = 0;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ---------------- Init Views ----------------
        tvUsername = findViewById(R.id.tvUsername);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        btnNotification = findViewById(R.id.btnNotification);
        etSearch = findViewById(R.id.etSearch);
        gridCategories = findViewById(R.id.gridCategories);
        ivPopularBanner = findViewById(R.id.ivPopularBanner);

        // ---------------- Setup Base ----------------
        setupBottomNavigation(R.id.nav_home);
        setupDrawer();

        // ---------------- SQLite ----------------
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // ---------------- Load Data ----------------
        setupBanner();
        loadCategoriesFromSQLite();

        // ---------------- Buttons ----------------
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(findViewById(R.id.navigationView)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));

        // ---------------- Search ----------------
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    searchCategories(keyword);
                } else {
                    loadCategoriesFromSQLite(); // reload all if search is empty
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ---------------- Banner ----------------
    private void setupBanner() {
        bannerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentBannerIndex++;
                if (currentBannerIndex >= bannerImages.length) {
                    currentBannerIndex = 0;
                }
                ivPopularBanner.setImageResource(bannerImages[currentBannerIndex]);
                bannerHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    // ---------------- Load Categories ----------------
    private void loadCategoriesFromSQLite() {
        List<Category> categories = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT id, title, image FROM categories",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String image = cursor.getString(2);

                categories.add(new Category(id, title, image));
            } while (cursor.moveToNext());
        }

        cursor.close();
        displayCategories(categories);
    }

    // ---------------- Search Categories ----------------
    private void searchCategories(String keyword) {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT id, title, image FROM categories WHERE title LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String image = cursor.getString(2);
                categories.add(new Category(id, title, image));
            } while (cursor.moveToNext());
        }

        cursor.close();
        displayCategories(categories);
    }

    // ---------------- Display Categories ----------------
    private void displayCategories(List<Category> categories) {
        gridCategories.removeAllViews();

        for (Category cat : categories) {

            var item = getLayoutInflater()
                    .inflate(R.layout.item_category, gridCategories, false);

            ImageView imgCat = item.findViewById(R.id.imgCategory);
            TextView txtCat = item.findViewById(R.id.txtCategory);

            txtCat.setText(cat.title);

            int imgResId = getResources()
                    .getIdentifier(cat.image, "drawable", getPackageName());

            if (imgResId != 0) {
                imgCat.setImageResource(imgResId);
            }

            // Navigate to CategoryActivity
            item.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
                intent.putExtra("categoryId", cat.id);
                intent.putExtra("categoryTitle", cat.title);
                intent.putExtra("categoryImage", cat.image);
                startActivity(intent);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            item.setLayoutParams(params);

            gridCategories.addView(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacksAndMessages(null);
        db.close();
    }
}
