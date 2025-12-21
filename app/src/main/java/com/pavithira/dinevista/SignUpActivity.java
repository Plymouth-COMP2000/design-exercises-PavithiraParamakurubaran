package com.pavithira.dinevista;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pavithira.dinevista.models.User;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmailSign, etPasswordSign, etConfirmPassword;
    private com.google.android.material.button.MaterialButton btnSignUp;
    private ImageButton btnBack;
    private ImageView bottomIcon;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmailSign = findViewById(R.id.etEmailSign);
        etPasswordSign = findViewById(R.id.etPasswordSign);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);
        bottomIcon = findViewById(R.id.bottomIcon);

        // Back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SelectAuthActivity.class));
            finish();
        });

        // Accessibility icon
        bottomIcon.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, AccessibilityActivity.class));
        });

        // Sign up button
        btnSignUp.setOnClickListener(v -> createAccount());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }

    }

    private void createAccount() {
        String name = etName.getText().toString().trim();
        String email = etEmailSign.getText().toString().trim();
        String password = etPasswordSign.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Create Firebase Auth User
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get the Firebase UID
                        String firebaseUid = mAuth.getCurrentUser().getUid();

                        // Step 2: Generate sequential custom ID
                        db.collection("users")
                                .orderBy("customId", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    String nextId; // default if no users
                                    if (!querySnapshot.isEmpty()) {
                                        DocumentSnapshot lastUser = querySnapshot.getDocuments().get(0);
                                        String lastId = lastUser.getString("customId");
                                        int idNum = Integer.parseInt(lastId);
                                        idNum++;
                                        nextId = String.format("%02d", idNum); // format as 01, 02...
                                    } else {
                                        nextId = "01";
                                    }

                                    // Step 3: Save user to Firestore with customId
                                    User user = new User(
                                            firebaseUid, email, password, name, "", "", "", "guest"
                                    );

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("uid", firebaseUid);
                                    userMap.put("customId", nextId); // your sequential ID
                                    userMap.put("name", name);
                                    userMap.put("email", email);
                                    userMap.put("password", password);
                                    userMap.put("role", "guest");
                                    userMap.put("phone", null);

                                    userMap.put("customId", nextId); // e.g., "01"
                                    db.collection("users")
                                            .document(nextId) // document ID = custom ID
                                            .set(userMap)
                                            .addOnSuccessListener(aVoid -> {
                                                // Save custom ID locally for later use
                                                getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                                        .edit()
                                                        .putString("CUSTOM_ID", nextId)
                                                        .apply();

                                                Toast.makeText(SignUpActivity.this,
                                                        "Account Created! Your ID: " + nextId,
                                                        Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignUpActivity.this, SelectAuthActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SignUpActivity.this,
                                                        "Failed to save user info: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this,
                                            "Failed to fetch last user ID: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {
                        // Auth creation failed
                        Toast.makeText(SignUpActivity.this,
                                "Failed to create account: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}
