package com.pavithira.dinevista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pavithira.dinevista.models.User;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private ImageButton btnBack;
    private LinearLayout signUpLayout;
    private ImageView bottomIcon;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnBack = findViewById(R.id.btnBack);
        signUpLayout = findViewById(R.id.signUpLayout);
        bottomIcon = findViewById(R.id.bottomIcon);

        // Back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SelectAuthActivity.class));
            finish();
        });

        // Sign Up navigation
        signUpLayout.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });

        // Accessibility icon
        bottomIcon.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, AccessibilityActivity.class));
        });

        btnSignIn.setOnClickListener(v -> loginUser());


    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                        String dbPassword = doc.getString("password");
                        String role = doc.getString("role");

                        // 🔑 CUSTOM ID = document ID
                        String customId = doc.getId();

                        if (password.equals(dbPassword)) {

                            // ✅ SAVE CUSTOM ID
                            getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                    .edit()
                                    .putString("CUSTOM_ID", customId)
                                    .apply();

                            // ✅ UPDATE FCM TOKEN AFTER LOGIN
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnSuccessListener(token -> {
                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(customId)
                                                .update("fcmToken", token);
                                    });

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            // 🚀 NAVIGATION
                            if ("guest".equalsIgnoreCase(role)) {
                                startActivity(new Intent(this, HomeActivity.class));
                            } else if ("staff".equalsIgnoreCase(role)) {
                                startActivity(new Intent(this, StaffHomeActivity.class));
                            }

                            finish();

                        } else {
                            Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "No account found with this email!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


}
