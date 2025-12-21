package com.pavithira.dinevista;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends BaseActivity {

    private GridLayout gridCategories;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        setupBottomNavigation(R.id.nav_menu);
        setupDrawer();

        gridCategories = findViewById(R.id.gridCategories);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        loadCategoriesFromSQLite();
    }

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

    private void displayCategories(List<Category> categories) {
        gridCategories.removeAllViews();

        for (Category cat : categories) {

            View item = getLayoutInflater()
                    .inflate(R.layout.item_category, gridCategories, false);

            ImageView imgCat = item.findViewById(R.id.imgCategory);
            TextView txtCat = item.findViewById(R.id.txtCategory);

            txtCat.setText(cat.title);

            int imgResId = getResources()
                    .getIdentifier(cat.image, "drawable", getPackageName());

            if (imgResId != 0) {
                imgCat.setImageResource(imgResId);
            }

            item.setOnClickListener(v -> {
                Intent intent = new Intent(MenuActivity.this, CategoryActivity.class);
                intent.putExtra("categoryId", cat.id);        // INTEGER now
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
}
