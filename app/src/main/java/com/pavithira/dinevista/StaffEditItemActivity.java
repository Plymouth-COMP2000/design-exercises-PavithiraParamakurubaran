package com.pavithira.dinevista;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StaffEditItemActivity extends StaffBaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imgUploadPreview;
    private EditText etItemName, etFoodDescription, etPrice;
    private Spinner spinnerFoodCategory;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private List<Integer> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    private int foodId; // Food ID to edit
    private String selectedImageName; // Current or new image

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_add_new_item); // same layout as add item

        // ---------------- Setup Base ----------------
        setupBottomNavigation(R.id.nav_menu);
        setupDrawer();


        imgUploadPreview = findViewById(R.id.imgUploadPreview);
        etItemName = findViewById(R.id.etItemName);
        etFoodDescription = findViewById(R.id.etFoodDescription);
        etPrice = findViewById(R.id.etPrice);
        spinnerFoodCategory = findViewById(R.id.spinnerFoodCategory);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Get food ID from intent
        foodId = getIntent().getIntExtra("foodId", -1);
        if (foodId == -1) {
            Toast.makeText(this, "Invalid food item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCategoriesFromDatabase();
        loadFoodData(foodId);

        // Image click to select new image
        imgUploadPreview.setOnClickListener(v -> openGalleryForImage());

        btnSave.setOnClickListener(v -> updateFoodItem());
    }

    private void loadCategoriesFromDatabase() {
        Cursor cursor = db.rawQuery("SELECT id, title FROM categories", null);
        categoryIds.clear();
        categoryNames.clear();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);

                categoryIds.add(id);
                categoryNames.add(title);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFoodCategory.setAdapter(adapter);
    }

    private void loadFoodData(int id) {
        Cursor cursor = db.rawQuery("SELECT title, description, image, price, category_id FROM foods WHERE id = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            String title = cursor.getString(0);
            String description = cursor.getString(1);
            selectedImageName = cursor.getString(2);
            double price = cursor.getDouble(3);
            int categoryId = cursor.getInt(4);

            etItemName.setText(title);
            etFoodDescription.setText(description);
            etPrice.setText(String.valueOf(price));
            // Display image
            int imgResId = getResources().getIdentifier(selectedImageName, "drawable", getPackageName());
            if (imgResId == 0) imgResId = R.drawable.imgplaceholder;
            imgUploadPreview.setImageResource(imgResId);

            // Set spinner selection
            int spinnerPos = categoryIds.indexOf(categoryId);
            if (spinnerPos >= 0) spinnerFoodCategory.setSelection(spinnerPos);
        }
        cursor.close();
    }

    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imgUploadPreview.setImageURI(imageUri);

            // Get image name
            String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                selectedImageName = cursor.getString(columnIndex);
                cursor.close();
            }
        }
    }

    private void updateFoodItem() {
        String name = etItemName.getText().toString().trim();
        String description = etFoodDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryPosition = spinnerFoodCategory.getSelectedItemPosition();
        int categoryId = categoryIds.get(categoryPosition);

        String updateQuery = "UPDATE foods SET title = ?, description = ?, image = ?, price = ?, category_id = ? WHERE id = ?";
        db.execSQL(updateQuery, new Object[]{name, description, selectedImageName, price, categoryId, foodId});

        Toast.makeText(this, "Food item updated successfully!", Toast.LENGTH_SHORT).show();
        finish(); // Close activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
