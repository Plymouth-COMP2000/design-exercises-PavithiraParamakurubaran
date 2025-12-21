package com.pavithira.dinevista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class GetStartedActivity extends AppCompatActivity {

    private MaterialButton getStartedBtn;
    private ImageView accessibilityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started); // make sure XML filename matches

        // Initialize views
        getStartedBtn = findViewById(R.id.getStartedBtn);
        accessibilityBtn = findViewById(R.id.accessibilityBtn);

        // Click listener for "Continue" button
        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SelectAuthActivity
                Intent intent = new Intent(GetStartedActivity.this, SelectAuthActivity.class);
                startActivity(intent);
            }
        });

// 🔹 Seed SQLite ONLY ON FIRST APP LAUNCH
        SharedPreferences prefs = getSharedPreferences("db", MODE_PRIVATE);
        if (!prefs.getBoolean("seeded", false)) {
            startActivity(new Intent(this, SQLiteSetupActivity.class));
            prefs.edit().putBoolean("seeded", true).apply();
        }
        // Click listener for accessibility button
        accessibilityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AccessibilityActivity
                Intent intent = new Intent(GetStartedActivity.this, AccessibilityActivity.class);
                startActivity(intent);
            }
        });
    }
}
