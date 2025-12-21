package com.pavithira.dinevista;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StaffReservation extends StaffBaseActivity {

    private LinearLayout reservationContainer;
    private TextView tvAll, tvUpcoming, tvCompleted, tvCancelled;
    private EditText etSearch;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_reservation);

        // Setup base activity components
        setupBottomNavigation(R.id.nav_reservations);
        setupDrawer();

        // Initialize views
        reservationContainer = findViewById(R.id.reservationContainer); // this should exist in XML
        etSearch = findViewById(R.id.etSearch);

        tvAll = findViewById(R.id.tvAll);
        tvUpcoming = findViewById(R.id.tvUpcoming);
        tvCompleted = findViewById(R.id.tvCompleted);
        tvCancelled = findViewById(R.id.tvCancelled);

        // Database
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // Filter click listeners
        tvAll.setOnClickListener(v -> {
            currentFilter = "all";
            loadReservations();
        });

        tvUpcoming.setOnClickListener(v -> {
            currentFilter = "upcoming";
            loadReservations();
        });

        tvCompleted.setOnClickListener(v -> {
            currentFilter = "completed";
            loadReservations();
        });

        tvCancelled.setOnClickListener(v -> {
            currentFilter = "cancelled";
            loadReservations();
        });

        // Search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                loadReservations();
            }
        });

        // Load reservations initially
        loadReservations();
    }

    private void loadReservations() {
        reservationContainer.removeAllViews();
        String searchText = etSearch.getText().toString().trim();

        String query = "SELECT name, date, time, status FROM reservations";
        boolean hasWhere = false;

        if (!currentFilter.equals("all")) {
            query += " WHERE status='" + currentFilter + "'";
            hasWhere = true;
        }

        if (!searchText.isEmpty()) {
            query += hasWhere ? " AND " : " WHERE ";
            query += "(name LIKE '%" + searchText + "%' OR date LIKE '%" + searchText + "%')";
        }

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String date = cursor.getString(1);
                String time = cursor.getString(2);
                String status = cursor.getString(3);
                addReservationItem(name, date, time, status);
            } while (cursor.moveToNext());
        } else {
            TextView tvNoData = new TextView(this);
            tvNoData.setText("No reservations found.");
            tvNoData.setPadding(16,16,16,16);
            reservationContainer.addView(tvNoData);
        }

        cursor.close();
    }

    private void addReservationItem(String name, String date, String time, String status) {
        // Inflate a reservation item layout (you can reuse your XML layout for each item)
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.reservation_item_layout, reservationContainer, false);

        TextView tvName = itemView.findViewById(R.id.tvName);
        TextView tvDate = itemView.findViewById(R.id.tvDate);
        TextView tvTime = itemView.findViewById(R.id.tvTime);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);

        tvName.setText("Name: " + name);
        tvDate.setText("Date: " + date);
        tvTime.setText("Time: " + time);
        tvStatus.setText("Status: " + status);

        reservationContainer.addView(itemView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
