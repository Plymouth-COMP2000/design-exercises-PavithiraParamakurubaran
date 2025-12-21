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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StaffAddNewItemActivity extends StaffBaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imgUploadPreview;
    private EditText etItemName, etFoodDescription, etPrice;
    private Spinner spinnerFoodCategory;
    private Button btnSave;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private List<Integer> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    private String selectedImageName = "default_food"; // default

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_add_new_item);

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

        loadCategoriesFromDatabase();

        // Click on image preview to select image
        imgUploadPreview.setOnClickListener(v -> openGalleryForImage());

        btnSave.setOnClickListener(v -> saveFoodItem());
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
                R.layout.spinner_item_white, categoryNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        spinnerFoodCategory.setAdapter(adapter);

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

            // Optional: Save image name or path for DB
            String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                selectedImageName = cursor.getString(columnIndex);
                cursor.close();
            }
        }
    }

    private void saveFoodItem() {
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

        // Insert into DB
        String insertQuery = "INSERT INTO foods (category_id, title, description, image, price) " +
                "VALUES (" + categoryId + ", '" + name + "', '" + description + "', '" + selectedImageName + "', " + price + ")";
        db.execSQL(insertQuery);

        Toast.makeText(this, "Food item added successfully!", Toast.LENGTH_SHORT).show();

        // Reset fields
        etItemName.setText("");
        etFoodDescription.setText("");
        etPrice.setText("");
        spinnerFoodCategory.setSelection(0);
        imgUploadPreview.setImageResource(R.drawable.imgplaceholder);
        selectedImageName = "default_food";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
