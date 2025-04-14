package com.example.inkturnejava;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inkturnejava.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SalonAppointmentsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private SalonAppointmentAdapter adapter;
    private List<Appointment> appointments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_appointments);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.salonAppointmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SalonAppointmentAdapter(appointments, (appointment, newStatus) -> {
            String documentId = createDocumentId(appointment.getSalonId(), appointment.getTimestamp());

            firestore.collection("appointments").document(documentId)
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Foglalás státusz: " + newStatus, Toast.LENGTH_SHORT).show();
                        loadAppointments();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SALON_APPT", "Nem sikerült frissíteni a foglalás státuszát", e);
                        Toast.makeText(this, "Hiba a státusz frissítésekor.", Toast.LENGTH_SHORT).show();
                    });
        });

        recyclerView.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        String salonId = auth.getCurrentUser().getUid();
        firestore.collection("appointments")
                .whereEqualTo("salonId", salonId)
                .get()
                .addOnSuccessListener(query -> {
                    appointments.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Appointment appt = doc.toObject(Appointment.class);
                        appointments.add(appt);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a foglalások lekérdezésekor.", Toast.LENGTH_SHORT).show();
                    Log.e("SALON_APPT", "Hiba: ", e);
                });
    }

    private String createDocumentId(String salonId, String timestamp) {
        return salonId + "_" + timestamp.replace(":", "-").replace(" ", "_");
    }
}