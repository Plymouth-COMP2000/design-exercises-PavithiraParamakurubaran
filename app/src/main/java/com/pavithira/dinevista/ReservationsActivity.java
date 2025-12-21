package com.pavithira.dinevista;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pavithira.dinevista.models.CartItem;
import com.pavithira.dinevista.models.Reservation;

import java.lang.reflect.Type;
import java.util.List;

public class ReservationsActivity extends BaseActivity {

    private LinearLayout reservationsContainer;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        setupBottomNavigation(R.id.nav_reservations);
        setupDrawer();

        reservationsContainer = findViewById(R.id.reservationsContainer);

        // Initialize SQLite
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        gson = new Gson();

        loadUserReservations();
    }

    private void loadUserReservations() {
        String customId = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", "guest");

        if (customId == null) {
            Toast.makeText(this, "User custom ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query SQLite for reservations
        String query = "SELECT * FROM reservations WHERE custom_id=?";
        Cursor cursor = db.rawQuery(query, new String[]{customId});

        reservationsContainer.removeAllViews();

        if (cursor.getCount() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No reservations found.");
            emptyText.setTextSize(16);
            emptyText.setPadding(16, 16, 16, 16);
            reservationsContainer.addView(emptyText);
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            Reservation reservation = new Reservation();
            reservation.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            reservation.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            reservation.email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            reservation.phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            reservation.pax = cursor.getString(cursor.getColumnIndexOrThrow("pax"));
            reservation.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            reservation.time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            reservation.request = cursor.getString(cursor.getColumnIndexOrThrow("request"));
            reservation.status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            reservation.subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"));

            // Deserialize cart items
            String cartJson = cursor.getString(cursor.getColumnIndexOrThrow("cart_items"));
            Type listType = new TypeToken<List<CartItem>>(){}.getType();
            reservation.items = gson.fromJson(cartJson, listType);

            addReservationItem(reservation);
        }
        cursor.close();
    }

    private void addReservationItem(Reservation reservation) {
        View item = getLayoutInflater().inflate(R.layout.item_reservation, reservationsContainer, false);

        TextView tvName = item.findViewById(R.id.tvName);
        TextView tvDate = item.findViewById(R.id.tvDate);
        TextView tvTime = item.findViewById(R.id.tvTime);
        TextView tvStatus = item.findViewById(R.id.tvStatus);

        LinearLayout btnView = item.findViewById(R.id.btnView);
        LinearLayout btnEdit = item.findViewById(R.id.btnEdit);
        LinearLayout btnCancel = item.findViewById(R.id.btnCancel);

        tvName.setText("Name: " + reservation.name);
        tvDate.setText("Date: " + reservation.date);
        tvTime.setText("Time: " + reservation.time);
        tvStatus.setText("Status: " + reservation.status);

        // Status color
        switch (reservation.status.toLowerCase()) {
            case "pending":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "completed":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "cancelled":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnView.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
        }

        // View reservation
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreviewReservationActivity.class);
            intent.putExtra("reservationId", reservation.id);
            intent.putExtra("name", reservation.name);
            intent.putExtra("email", reservation.email);
            intent.putExtra("phone", reservation.phone);
            intent.putExtra("pax", reservation.pax);
            intent.putExtra("date", reservation.date);
            intent.putExtra("time", reservation.time);
            intent.putExtra("request", reservation.request);
            intent.putExtra("subtotal", reservation.subtotal);
            intent.putExtra("isEdit", false);
            startActivity(intent);
        });

// Edit reservation
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("reservationId", reservation.id);
            intent.putExtra("name", reservation.name);
            intent.putExtra("email", reservation.email);
            intent.putExtra("phone", reservation.phone);
            intent.putExtra("pax", reservation.pax);
            intent.putExtra("date", reservation.date);
            intent.putExtra("time", reservation.time);
            intent.putExtra("request", reservation.request);
            intent.putExtra("subtotal", reservation.subtotal);
            intent.putExtra("cartItems", new java.util.ArrayList<>(reservation.items));
            startActivity(intent);
        });


        // Cancel reservation
        btnCancel.setOnClickListener(v -> {
            if (reservation.status.equalsIgnoreCase("cancelled")) {
                Toast.makeText(this, "Reservation already cancelled.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update status in SQLite
            ContentValues values = new ContentValues();
            values.put("status", "cancelled");

            int updated = db.update(
                    "reservations",
                    values,
                    "id=?",
                    new String[]{String.valueOf(reservation.id)}
            );


            if (updated > 0) {
                tvStatus.setText("Status: Cancelled");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnEdit.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                Toast.makeText(this, "Reservation cancelled successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to cancel reservation", Toast.LENGTH_SHORT).show();
            }
        });

        reservationsContainer.addView(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
