package com.pavithira.dinevista.api;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pavithira.dinevista.models.User;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Callback interface
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Create User
    public static void createUser(User user, FirestoreCallback callback) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("password", user.getPassword());
        userMap.put("name", user.getName());
        userMap.put("address", user.getAddress());
        userMap.put("username", user.getUsername());
        userMap.put("phone", user.getPhone());
        userMap.put("role", user.getRole());

        db.collection("users") // Firestore collection
                .document(user.getUid()) // Use username as unique document ID
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User added successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user", e);
                    callback.onFailure(e.getMessage());
                });
    }
}
