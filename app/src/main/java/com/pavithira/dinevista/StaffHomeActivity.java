package com.pavithira.dinevista;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

public class StaffHomeActivity extends StaffBaseActivity {

    // Database
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    // Statistics (ONLY THESE 3)
    private TextView tvPending, tvUpcoming, tvCompleted;

    // Pending reservation card
    private TextView tvName, tvDate, tvTime, tvStatus;
    private LinearLayout btnAccept, btnReject;

    private int reservationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        // ---------------- DB ----------------
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

// ---------------- Setup Base ----------------
        setupBottomNavigation(R.id.nav_home);
        setupDrawer();
        // ---------------- STAT VIEWS ----------------
        tvPending   = findViewById(R.id.tvPendingCount);
        tvUpcoming  = findViewById(R.id.tvUpcomingCount);
        tvCompleted = findViewById(R.id.tvCompletedCount);

        // ---------------- PENDING CARD ----------------
        tvName   = findViewById(R.id.tvUrgentName);
        tvDate   = findViewById(R.id.tvUrgentDate);
        tvTime   = findViewById(R.id.tvUrgentTime);
        tvStatus = findViewById(R.id.tvUrgentStatus);

        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        // ---------------- LOAD DATA ----------------
        loadReservationStats();
        loadPendingReservation();

        // ---------------- ACTIONS ----------------
        btnAccept.setOnClickListener(v -> updateStatus("upcoming"));
        btnReject.setOnClickListener(v -> updateStatus("cancelled"));
    }

    // ================= STATISTICS =================
    private void loadReservationStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            final String pending = getCount("pending");
            final String upcoming = getCount("upcoming");
            final String completed = getCount("completed");

            runOnUiThread(() -> {
                tvPending.setText(pending);
                tvUpcoming.setText(upcoming);
                tvCompleted.setText(completed);
            });
        });
    }

    private String getCount(String status) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM reservations WHERE LOWER(status)=?",
                new String[]{status}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return String.valueOf(count);
    }


    // ================= PENDING CARD =================
    private void loadPendingReservation() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Cursor cursor = db.rawQuery(
                    "SELECT id, name, date, time, status FROM reservations WHERE LOWER(status)='pending' ORDER BY id DESC LIMIT 1",
                    null
            );

            final boolean hasReservation = cursor.moveToFirst();
            final int id = hasReservation ? cursor.getInt(0) : -1;
            final String name = hasReservation ? cursor.getString(1) : "";
            final String date = hasReservation ? cursor.getString(2) : "";
            final String time = hasReservation ? cursor.getString(3) : "";
            final String status = hasReservation ? cursor.getString(4) : "";

            cursor.close();

            runOnUiThread(() -> {
                reservationId = id;
                if (hasReservation) {
                    tvName.setText("Name: " + name);
                    tvDate.setText("Date: " + date);
                    tvTime.setText("Time: " + time);
                    tvStatus.setText("Status: " + status);
                } else {
                    tvName.setText("No pending reservations");
                    tvDate.setText("");
                    tvTime.setText("");
                    tvStatus.setText("");
                }
            });
        });
    }


    // ================= UPDATE STATUS =================
    private void updateStatus(String status) {
        if (reservationId == -1) {
            Toast.makeText(this, "No pending reservation", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            db.execSQL(
                    "UPDATE reservations SET status=? WHERE id=?",
                    new Object[]{status, reservationId}
            );

            runOnUiThread(() -> {
                Toast.makeText(
                        this,
                        status.equals("upcoming") ? "Reservation accepted" : "Reservation cancelled",
                        Toast.LENGTH_SHORT
                ).show();
                loadReservationStats();
                loadPendingReservation();
            });
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
