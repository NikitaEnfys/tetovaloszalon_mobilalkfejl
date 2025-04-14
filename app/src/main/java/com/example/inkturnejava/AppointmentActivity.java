package com.example.inkturnejava;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inkturnejava.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AppointmentActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private AvailabilityManager availabilityManager;

    private Spinner timeSpinner;
    private EditText messageInput;
    private Button submitButton;

    private String salonId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        availabilityManager = new AvailabilityManager();

        timeSpinner = findViewById(R.id.timeSpinner);
        messageInput = findViewById(R.id.messageInput);
        submitButton = findViewById(R.id.submitAppointmentButton);

        salonId = getIntent().getStringExtra("salonId");
        if (salonId == null) {
            Toast.makeText(this, "Hiányzó szalon azonosító.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAvailableTimes();

        submitButton.setOnClickListener(v -> submitAppointment());
    }

    private void loadAvailableTimes() {
        availabilityManager.getAvailableTimes(salonId, times -> {
            if (times.isEmpty()) {
                Toast.makeText(this, "Nincs elérhető időpont.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(adapter);
        });
    }

    private String encodeTime(String time) {
        return time.replace(":", "-").replace(" ", "_");
    }

    private void submitAppointment() {
        FirebaseUser user = auth.getCurrentUser();
        String selectedTime = (String) timeSpinner.getSelectedItem();
        String message = messageInput.getText().toString().trim();

        if (user == null || selectedTime == null || selectedTime.isEmpty()) {
            Toast.makeText(this, "Válassz időpontot és írj megjegyzést!", Toast.LENGTH_SHORT).show();
            return;
        }

        Appointment appointment = new Appointment(
                salonId,
                user.getUid(),
                user.getEmail(),
                selectedTime,
                message,
                "pending"
        );

        String documentId = salonId + "_" + encodeTime(selectedTime);

        firestore.collection("appointments")
                .document(documentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Ez az időpont már foglalt.", Toast.LENGTH_SHORT).show();
                    } else {
                        firestore.collection("appointments")
                                .document(documentId)
                                .set(appointment)
                                .addOnSuccessListener(aVoid -> {
                                    availabilityManager.removeAvailability(salonId, selectedTime, () -> {
                                        Toast.makeText(this, "Sikeres foglalás!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Nem sikerült menteni a foglalást.", Toast.LENGTH_SHORT).show();
                                    Log.e("APPOINTMENT", "Mentési hiba", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nem sikerült ellenőrizni a foglalást.", Toast.LENGTH_SHORT).show();
                    Log.e("APPOINTMENT", "Ellenőrzési hiba", e);
                });
    }
}
