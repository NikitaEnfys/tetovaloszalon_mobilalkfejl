package com.example.inkturnejava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.inkturnejava.Salon;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userRole;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        TextView welcomeText = findViewById(R.id.welcomeText);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        container = findViewById(R.id.styleSectionContainer);

        String username = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Felhasználó";
        welcomeText.setText("Üdv, " + username + "!");

        getUserRole(role -> userRole = role);

        setupStyleSections();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(this, "Már a főoldalon vagy!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_profile) {
                if (userRole == null) {
                    Toast.makeText(this, "Adatok betöltése... Kérlek próbáld újra!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (userRole.equals("tattoo_salon")) {
                    startActivity(new Intent(this, SalonProfileActivity.class));
                } else {
                    startActivity(new Intent(this, CustomerProfileActivity.class));
                }
                return true;
            } else if (id == R.id.menu_appointments) {
                if (userRole == null) {
                    Toast.makeText(this, "Adatok betöltése... Kérlek próbáld újra!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (userRole.equals("tattoo_salon")) {
                    startActivity(new Intent(this, SalonAppointmentsActivity.class));
                } else {
                    startActivity(new Intent(this, CustomerAppointmentsActivity.class));
                }
                return true;
        } else if (id == R.id.menu_salons) {
                startActivity(new Intent(this, SalonListActivity.class));
                return true;
            }
            return false;
        });
    }

    private interface RoleCallback {
        void onRoleFetched(String role);
    }

    private void getUserRole(@NonNull RoleCallback callback) {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        callback.onRoleFetched(role);
                    } else {
                        callback.onRoleFetched(null);
                    }
                })
                .addOnFailureListener(e -> callback.onRoleFetched(null));
    }

    private void setupStyleSections() {
        String[] styles = {"minimal", "realistic", "traditional"};
        for (String style : styles) {
            loadStyleSection(style);
        }
    }

    private void loadStyleSection(String style) {
        firestore.collection("tattoo_salons")
                .whereEqualTo("style", style)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Salon> salons = new ArrayList<>();
                    List<String> docIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        salons.add(doc.toObject(Salon.class));
                        docIds.add(doc.getId());
                    }

                    StyleSectionView sectionView = new StyleSectionView(this);
                    sectionView.setData(style, salons, docIds);
                    container.addView(sectionView);
                });
    }

}