package com.example.inkturnejava;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inkturnejava.AvailabilityManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalonProfileActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private AvailabilityManager availabilityManager;

    private EditText nameEditText, addressEditText, descriptionEditText, openingHoursEditText;
    private Button saveButton, logoutButton, addTimeButton;
    private RecyclerView availabilityList;
    private AvailabilityAdapter adapter;
    private List<String> availableTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_profile);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        availabilityManager = new AvailabilityManager();

        nameEditText = findViewById(R.id.salonNameEditText);
        addressEditText = findViewById(R.id.salonAddressEditText);
        descriptionEditText = findViewById(R.id.salonDescriptionEditText);
        openingHoursEditText = findViewById(R.id.salonOpeningHoursEditText);
        saveButton = findViewById(R.id.saveSalonDataButton);
        logoutButton = findViewById(R.id.logoutButton);
        addTimeButton = findViewById(R.id.addAvailabilityTimeButton);
        availabilityList = findViewById(R.id.availabilityRecyclerView);
        availabilityList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AvailabilityAdapter(availableTimes, time -> {
            availabilityManager.removeAvailability(auth.getCurrentUser().getUid(), time, () ->
                    Toast.makeText(this, "Törölt időpont: " + time, Toast.LENGTH_SHORT).show());
        });
        availabilityList.setAdapter(adapter);

        loadSalonData();
        setupListeners();
        listenToAvailabilityChanges();
    }

    private void loadSalonData() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        firestore.collection("tattoo_salons").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        nameEditText.setText(snapshot.getString("name"));
                        addressEditText.setText(snapshot.getString("address"));
                        descriptionEditText.setText(snapshot.getString("description"));
                        openingHoursEditText.setText(snapshot.getString("openingHours"));
                    }
                });
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            Map<String, Object> salonData = new HashMap<>();
            salonData.put("name", nameEditText.getText().toString());
            salonData.put("address", addressEditText.getText().toString());
            salonData.put("description", descriptionEditText.getText().toString());
            salonData.put("openingHours", openingHoursEditText.getText().toString());
            salonData.put("ownerId", auth.getCurrentUser().getUid());


            firestore.collection("tattoo_salons")
                    .document(auth.getCurrentUser().getUid())
                    .set(salonData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Sikeresen mentve!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Kijelentkezés", Toast.LENGTH_SHORT).show();
            startActivity(new android.content.Intent(this, MainActivity.class)
                    .setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        addTimeButton.setOnClickListener(v -> {
            Log.d("AVAILABILITY", "Kattintott a hozzáadás gombra");

            Calendar now = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (timeView, hour, minute) -> {
                                    Calendar selected = Calendar.getInstance();
                                    selected.set(year, month, day, hour, minute);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    String formatted = sdf.format(selected.getTime());

                                    availabilityManager.addAvailability(auth.getCurrentUser().getUid(), formatted, () ->
                                            Toast.makeText(this, "Hozzáadott időpont: " + formatted, Toast.LENGTH_SHORT).show());
                                },
                                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                        timePicker.show();
                    },
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });
    }

    private void listenToAvailabilityChanges() {
        availabilityManager.listenToChanges(auth.getCurrentUser().getUid(), times -> {
            availableTimes.clear();
            availableTimes.addAll(times);
            adapter.notifyDataSetChanged();
        });
    }
}
