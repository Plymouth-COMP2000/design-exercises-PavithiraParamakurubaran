package com.pavithira.dinevista;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pavithira.dinevista.models.CartItem;
import com.google.gson.Gson;
import com.pavithira.dinevista.models.Reservation;

import java.util.List;

public class PreviewReservationActivity extends BaseActivity {

    private TextView tvName, tvEmail, tvPhone, tvPax, tvDate, tvTime, tvRequest, tvSubtotal;
    private LinearLayout cartContainer;
    private boolean isEdit = false;
    private int reservationId = -1;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_reservation);

        setupBottomNavigation(R.id.nav_cart);
        setupDrawer();

        // Init SQLite
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Bind views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvPax = findViewById(R.id.tvPax);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvRequest = findViewById(R.id.tvRequest);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        cartContainer = findViewById(R.id.cartItemsContainer);

        // Check if editing
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        reservationId = getIntent().getIntExtra("reservationId", -1);

        // Load reservation data if available
        receiveData();

        Button btnProceed = findViewById(R.id.btnProceed);
        btnProceed.setOnClickListener(v -> saveReservation());
    }

    public static Intent createIntent(Context context, Reservation reservation, boolean isEdit) {
        Intent intent = new Intent(context, PreviewReservationActivity.class);
        intent.putExtra("reservationId", reservation.id);
        intent.putExtra("name", reservation.name);
        intent.putExtra("email", reservation.email);
        intent.putExtra("phone", reservation.phone);
        intent.putExtra("pax", reservation.pax);
        intent.putExtra("date", reservation.date);
        intent.putExtra("time", reservation.time);
        intent.putExtra("request", reservation.request);
        intent.putExtra("subtotal", reservation.subtotal);
        intent.putExtra("isEdit", isEdit);
        return intent;
    }

    private void receiveData() {
        if (getIntent() == null) return;

        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String pax = getIntent().getStringExtra("pax");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String request = getIntent().getStringExtra("request");
        double subtotal = getIntent().getDoubleExtra("subtotal", 0.0);

        // Populate cart dynamically
        List<CartItem> items = CartManager.getInstance().getItems();
        cartContainer.removeAllViews();
        for (CartItem item : items) {
            View row = getLayoutInflater().inflate(R.layout.row_cart_item_preview, cartContainer, false);
            TextView txtName = row.findViewById(R.id.txtItemName);
            TextView txtQty = row.findViewById(R.id.txtQty);
            TextView txtPrice = row.findViewById(R.id.txtPrice);

            txtName.setText(item.getName());
            txtQty.setText(String.valueOf(item.getQty()));
            txtPrice.setText("RM " + String.format("%.2f", item.getTotalPrice()));

            cartContainer.addView(row);
        }

        // Set reservation data to views
        tvName.setText(name);
        tvEmail.setText(email == null || email.isEmpty() ? "-" : email);
        tvPhone.setText(phone);
        tvPax.setText(pax);
        tvDate.setText(date);
        tvTime.setText(time);
        tvRequest.setText(request == null || request.isEmpty() ? "-" : request);
        tvSubtotal.setText("RM " + String.format("%.2f", subtotal));
    }

    private void saveReservation() {
        String name = tvName.getText().toString();
        String email = tvEmail.getText().toString();
        String phone = tvPhone.getText().toString();
        String pax = tvPax.getText().toString();
        String date = tvDate.getText().toString();
        String time = tvTime.getText().toString();
        String request = tvRequest.getText().toString();
        double subtotal = Double.parseDouble(tvSubtotal.getText().toString().replace("RM ", ""));

        String customId = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", "guest");

        // Serialize cart items as JSON
        Gson gson = new Gson();
        String cartJson = gson.toJson(CartManager.getInstance().getItems());

        ContentValues values = new ContentValues();
        values.put("custom_id", customId);
        values.put("name", name);
        values.put("email", email.equals("-") ? "" : email);
        values.put("phone", phone);
        values.put("pax", pax);
        values.put("date", date);
        values.put("time", time);
        values.put("request", request.equals("-") ? "" : request);
        values.put("subtotal", subtotal);
        values.put("status", "pending");
        values.put("cart_items", cartJson);

        long result = -1;
        if (isEdit && reservationId != -1) {
            result = db.update("reservations", values, "id=?", new String[]{String.valueOf(reservationId)});
            Log.d("Reservation", "Update result=" + result);
        } else {
            result = db.insert("reservations", null, values);
            Log.d("Reservation", "Insert result=" + result);
        }

        if (result != -1) {
            Toast.makeText(this, "Reservation saved!", Toast.LENGTH_SHORT).show();

            // ---------------- SEND NOTIFICATION ----------------
            sendReservationNotification(name);

        } else {
            Toast.makeText(this, "Failed to save reservation!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendReservationNotification(String userName) {
        String message = "Hi " + userName + ", your reservation is booked and waiting for approval.";

        // ---------------- 1. Save to SQLite ----------------
        SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("custom_id", getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", "guest"));
        cv.put("title", "Reservation Booked");
        cv.put("message", message);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("read", 0);
        writableDb.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, cv);

        // ---------------- 2. Show system notification ----------------
        String channelId = "reservation_channel";
        String channelName = "Reservation Notifications";

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Reservation Booked")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
