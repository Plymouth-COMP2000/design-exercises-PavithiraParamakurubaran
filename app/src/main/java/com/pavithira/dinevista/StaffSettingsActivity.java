package com.pavithira.dinevista;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class StaffSettingsActivity extends BaseActivity {

    private static final String CHANNEL_ID = "staff_notifications_channel";
    private TextView tvUsername, tvEmail;
    private Switch switchNewReservation, switchCancelation, switchAppUpdates;
    private Button btnChangePassword, btnHelp;

    private FirebaseFirestore db;
    private ListenerRegistration reservationListener;

    private String customId;
    private String username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // same layout

        setupDrawer();
        setupBottomNavigation(R.id.nav_settings);

        tvUsername = findViewById(R.id.tvDrawerName);
        tvEmail = findViewById(R.id.tvDrawerEmail);
        switchNewReservation = findViewById(R.id.switchNewReservation);
        switchCancelation = findViewById(R.id.switchCancelation);
        switchAppUpdates = findViewById(R.id.switchAppUpdates);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnHelp = findViewById(R.id.btnHelp);

        db = FirebaseFirestore.getInstance();
        createNotificationChannel();

        loadUserData(); // fetch using CUSTOM_ID
        setupSwitches();
        setupButtons();
        startReservationListener();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        customId = prefs.getString("CUSTOM_ID", null); // ✅ use CUSTOM_ID

        if (customId == null) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(customId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        username = doc.getString("name");
                        email = doc.getString("email");

                        tvUsername.setText(username);
                        tvEmail.setText(email);
                    }
                });
    }

    private void setupSwitches() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        switchNewReservation.setChecked(prefs.getBoolean("NOTIF_NEW_RESERVATION", true));
        switchCancelation.setChecked(prefs.getBoolean("NOTIF_CANCELATION", true));
        switchAppUpdates.setChecked(prefs.getBoolean("NOTIF_APP_UPDATES", true));

        switchNewReservation.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("NOTIF_NEW_RESERVATION", isChecked).apply());

        switchCancelation.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("NOTIF_CANCELATION", isChecked).apply());

        switchAppUpdates.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("NOTIF_APP_UPDATES", isChecked).apply());
    }

    private void setupButtons() {
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
        });

        btnHelp.setOnClickListener(v -> {
            startActivity(new android.content.Intent(StaffSettingsActivity.this, HelpActivity.class));
        });
    }

    private void startReservationListener() {
        if (customId == null) return;

        reservationListener = db.collection("reservations")
                .document(customId)
                .collection("userReservations") // same as normal settings
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) return;
                        if (snapshots == null || snapshots.isEmpty()) return;

                        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String status = doc.getString("status");
                            String name = doc.getString("name");

                            if ("pending".equalsIgnoreCase(status) && prefs.getBoolean("NOTIF_NEW_RESERVATION", true)) {
                                sendNotification("New Reservation", "Reservation by " + name + " is pending.");
                            }
                            if ("cancelled".equalsIgnoreCase(status) && prefs.getBoolean("NOTIF_CANCELATION", true)) {
                                sendNotification("Reservation Cancelled", "Reservation by " + name + " was cancelled.");
                            }
                        }
                    }
                });
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Staff Notifications";
            String description = "Notification channel for reservation events";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reservationListener != null) reservationListener.remove();
    }
}
