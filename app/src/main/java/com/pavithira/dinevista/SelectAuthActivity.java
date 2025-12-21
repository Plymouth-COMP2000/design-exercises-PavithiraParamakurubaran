package com.pavithira.dinevista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SelectAuthActivity extends AppCompatActivity {

    MaterialButton btnSignIn, btnSignUp;
    TextView tvGuest, tvSkip;
    ImageView bottomIcon;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_auth); // make sure this matches your XML

        // Initialize views
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGuest = findViewById(R.id.tvGuest);
        tvSkip = findViewById(R.id.tvSkip);
        bottomIcon = findViewById(R.id.bottomIcon);
        btnBack = findViewById(R.id.btnBack);

        // --- Button Click Listeners ---

        // Sign In → SignInActivity
        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, SignInActivity.class);
                startActivity(intent);
            });
        }

        // Sign Up → SignUpActivity
        if (btnSignUp != null) {
            btnSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, SignUpActivity.class);
                startActivity(intent);
            });
        }

        // Continue as Guest → HomeActivity
        if (tvGuest != null) {
            tvGuest.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // optional: close auth selection
            });
        }

        // Skip → Go back to GetStartedActivity
        if (tvSkip != null) {
            tvSkip.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, GetStartedActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Bottom-left Accessibility icon → AccessibilityActivity
        if (bottomIcon != null) {
            bottomIcon.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, AccessibilityActivity.class);
                startActivity(intent);
            });
        }

        // Optional: Back button (top-left) → Go back to GetStartedActivity
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(SelectAuthActivity.this, GetStartedActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
