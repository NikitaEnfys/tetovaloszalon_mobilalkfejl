package com.example.inkturnejava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerProfileActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private TextView nameTextView, emailTextView;
    private EditText phoneEditText, birthdayEditText;
    private Button saveButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneEditText = findViewById(R.id.phoneEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        saveButton = findViewById(R.id.saveProfileButton);
        logoutButton = findViewById(R.id.logoutButton);

        loadUserData();
        setupListeners();
    }

    private void loadUserData() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        nameTextView.setText("Név: " + snapshot.getString("name"));
                        emailTextView.setText("Email: " + snapshot.getString("email"));
                        phoneEditText.setText(snapshot.getString("phone") != null ? snapshot.getString("phone") : "");
                        birthdayEditText.setText(snapshot.getString("birthday") != null ? snapshot.getString("birthday") : "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Nem sikerült betölteni az adatokat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString();
            String birthday = birthdayEditText.getText().toString();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("phone", phone);
            updatedData.put("birthday", birthday);

            firestore.collection("users").document(auth.getCurrentUser().getUid())
                    .update(updatedData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Sikeres mentés!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Kijelentkezés", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
    }
}
