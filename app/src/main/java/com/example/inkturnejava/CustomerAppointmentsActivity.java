package com.example.inkturnejava;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerAppointmentsActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<com.example.inkturnejava.Appointment> appointments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_appointments);

        recyclerView = findViewById(R.id.customerAppointmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new AppointmentAdapter(appointments);
        recyclerView.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("appointments")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    appointments.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        com.example.inkturnejava.Appointment appt = doc.toObject(com.example.inkturnejava.Appointment.class);
                        appointments.add(appt);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("CUSTOMER_APPT", "Nem sikerült lekérni a foglalásokat", e);
                    Toast.makeText(this, "Hiba történt a foglalások lekérdezésekor.", Toast.LENGTH_SHORT).show();
                });
    }
}