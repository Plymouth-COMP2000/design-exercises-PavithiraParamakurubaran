package com.pavithira.dinevista;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class StaffReservationRequestActivity extends StaffBaseActivity {

    private LinearLayout reservationContainer;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_reservation_request);

        // ---------------- Setup Base ----------------
        setupBottomNavigation(R.id.nav_reservation_request);
        setupDrawer();

        reservationContainer = findViewById(R.id.reservationContainer);
        if (reservationContainer == null) {
            throw new RuntimeException("reservationContainer is missing in the layout!");
        }


        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        loadPendingReservations();
    }

    private void loadPendingReservations() {
        reservationContainer.removeAllViews();

        Cursor cursor = db.rawQuery(
                "SELECT id, name, date, time, status " +
                        "FROM reservations WHERE status='pending'", null);


        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1); // now correct
                String date = cursor.getString(2);
                String time = cursor.getString(3);
                String status = cursor.getString(4);


                addReservationCard(id, name, date, time, status);

            } while (cursor.moveToNext());
        } else {
            // No pending reservations
            TextView noData = new TextView(this);
            noData.setText("No pending reservations.");
            noData.setPadding(16,16,16,16);
            reservationContainer.addView(noData);
        }
        cursor.close();
    }

    private void addReservationCard(int id, String name, String date, String time, String status) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View card = inflater.inflate(R.layout.reservation_card_item, reservationContainer, false);

        TextView tvName = card.findViewById(R.id.tvName);
        TextView tvDate = card.findViewById(R.id.tvDate);
        TextView tvTime = card.findViewById(R.id.tvTime);
        TextView tvStatus = card.findViewById(R.id.tvStatus);

        tvName.setText("Name: " + name);
        tvDate.setText("Date: " + date);
        tvTime.setText("Time: " + time);
        tvStatus.setText("Status: " + status);

        // Accept
        LinearLayout btnAccept = card.findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(v -> {
            updateReservationStatus(id, "upcoming"); // Change status to "upcoming"
            reservationContainer.removeView(card);
            Toast.makeText(this, "Reservation marked as upcoming", Toast.LENGTH_SHORT).show();

            // --- Send notification to the user ---
            sendReservationAcceptedNotification(name);
        });

        // Reject
        LinearLayout btnReject = card.findViewById(R.id.btnReject);
        btnReject.setOnClickListener(v -> {
            updateReservationStatus(id, "rejected");
            reservationContainer.removeView(card);
            Toast.makeText(this, "Reservation rejected", Toast.LENGTH_SHORT).show();
        });

        reservationContainer.addView(card);
    }

    // ------------------- Notification Method -------------------
    private void sendReservationAcceptedNotification(String userName) {
        String message = "Hi " + userName + ", your reservation has been accepted!";

        // Save notification in SQLite (optional)
        ContentValues cv = new ContentValues();
        cv.put("custom_id", getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", "guest"));
        cv.put("title", "Reservation Accepted");
        cv.put("message", message);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("read", 0);
        db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, cv);

        // Show system notification
        String channelId = "reservation_channel";
        String channelName = "Reservation Notifications";

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Reservation Accepted")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void updateReservationStatus(int reservationId, String newStatus) {
        db.execSQL("UPDATE reservations SET status='" + newStatus + "' WHERE id=" + reservationId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}
