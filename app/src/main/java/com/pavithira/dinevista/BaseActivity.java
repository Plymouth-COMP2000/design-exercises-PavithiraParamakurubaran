package com.pavithira.dinevista;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    protected static final String CHANNEL_ID = "notifications_channel";

    protected BottomNavigationView bottomNav;
    protected DrawerLayout drawerLayout;

    protected ImageView btnNotification;

    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        requestNotificationPermission(); // ✅ ADD THIS
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    // ---------------- NOTIFICATION CHANNEL ----------------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reservation notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ---------------- BOTTOM NAVIGATION ----------------
    protected void setupBottomNavigation(final int currentMenuId) {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(currentMenuId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentMenuId) return true;

            if (id == R.id.nav_home) {
                openActivity(HomeActivity.class);
            } else if (id == R.id.nav_menu) {
                openActivity(MenuActivity.class);
            } else if (id == R.id.nav_cart) {
                openActivity(CartActivity.class);
            } else if (id == R.id.nav_reservations) {
                openActivity(ReservationsActivity.class);
            } else if (id == R.id.nav_settings) {
                openActivity(SettingsActivity.class);
            }

            return true;
        });
    }

    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout == null) return;

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> drawerLayout.closeDrawers());
        }

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> openActivity(NotificationActivity.class));
        }

        // ---------------- LOGOUT ----------------
        NavigationView navigationView = findViewById(R.id.navigationView);
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                LinearLayout logoutLayout = headerView.findViewById(R.id.Logout);
                if (logoutLayout != null) {
                    logoutLayout.setOnClickListener(v -> {
                        // Clear preferences
                        getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit().clear().apply();
                        // Sign out Firebase
                        if (mAuth != null) mAuth.signOut();
                        // Go to SelectAuthActivity
                        Intent intent = new Intent(BaseActivity.this, SelectAuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });
                }
            }
        }

        loadUserInfo();
    }


    // ---------------- OPEN ACTIVITY HELPER ----------------
    protected void openActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(0, 0);
        if (drawerLayout != null) drawerLayout.closeDrawers();
    }

    // ---------------- LOGOUT ----------------
    protected void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, SelectAuthActivity.class));
        finish();
    }

    // ---------------- LOAD USER INFO ----------------
    private void loadUserInfo() {
        String customId = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("CUSTOM_ID", null);
        if (customId == null) return;

        db.collection("users").document(customId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    NavigationView navigationView = findViewById(R.id.navigationView);
                    if (navigationView == null) return;

                    View headerView = navigationView.getHeaderView(0);
                    if (headerView == null) return;

                    TextView tvName = headerView.findViewById(R.id.tvDrawerName);
                    TextView tvEmail = headerView.findViewById(R.id.tvDrawerEmail);

                    if (tvName != null)
                        tvName.setText(documentSnapshot.getString("name"));
                    if (tvEmail != null)
                        tvEmail.setText(documentSnapshot.getString("email"));
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
