package com.example.inkturnejava.auth;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onComplete(boolean success, String message);
    }

    public void registerUser(String email, String password, String role, String name, String style, @NonNull AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("role", role);
                        userData.put("name", name);

                        firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    if ("tattoo_salon".equals(role)) {
                                        Map<String, Object> salonData = new HashMap<>();
                                        salonData.put("name", name);
                                        salonData.put("ownerId", userId);
                                        salonData.put("address", "");
                                        salonData.put("description", "");
                                        salonData.put("openingHours", "");
                                        salonData.put("style", style != null ? style : "unknown");

                                        firestore.collection("tattoo_salons").document(userId).set(salonData);
                                    }

                                    callback.onComplete(true, "Sikeres regisztráció!");
                                })
                                .addOnFailureListener(e ->
                                        callback.onComplete(false, "Firestore hiba: " + e.getMessage()));

                    } else {
                        callback.onComplete(false, task.getException() != null ?
                                task.getException().getMessage() : "Ismeretlen hiba");
                    }
                });
    }

    public void loginUser(String email, String password, @NonNull AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true, "Sikeres bejelentkezés!");
                    } else {
                        String msg = "Ismeretlen hiba";
                        if (task.getException() instanceof FirebaseAuthException) {
                            msg = ((FirebaseAuthException) task.getException()).getErrorCode();
                        } else if (task.getException() != null) {
                            msg = task.getException().getMessage();
                        }
                        callback.onComplete(false, msg);
                    }
                });
    }

    // 🔹 KIJELENTKEZÉS
    public void logout() {
        auth.signOut();
    }
}
