package com.example.inkturnejava;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AvailabilityManager {

    private static final String TAG = "AvailabilityManager";
    private final FirebaseFirestore firestore;

    public AvailabilityManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getAvailableTimes(String salonId, Consumer<List<String>> callback) {
        firestore.collection("salon_availabilities")
                .document(salonId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("times")) {
                        List<String> times = (List<String>) snapshot.get("times");
                        Log.d(TAG, "Lekérdezett időpontok: " + times);
                        callback.accept(times);
                    } else {
                        Log.d(TAG, "Nincs times mező vagy dokumentum.");
                        callback.accept(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Hiba a szabad időpontok lekérésekor", e);
                    callback.accept(new ArrayList<>());
                });
    }

    public void addAvailability(String salonId, String timeSlot, Runnable onComplete) {
        DocumentReference ref = firestore.collection("salon_availabilities").document(salonId);

        Map<String, Object> data = new HashMap<>();
        data.put("times", FieldValue.arrayUnion(timeSlot));

        ref.set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(r -> {
                    Log.d(TAG, "Időpont mentve: " + timeSlot);
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Nem sikerült elmenteni az időpontot", e));
    }

    public void removeAvailability(String salonId, String timeSlot, Runnable onComplete) {
        firestore.collection("salon_availabilities")
                .document(salonId)
                .update("times", FieldValue.arrayRemove(timeSlot))
                .addOnSuccessListener(r -> {
                    Log.d(TAG, "Törölt időpont: " + timeSlot);
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Nem sikerült törölni az időpontot", e));
    }

    public ListenerRegistration listenToChanges(String salonId, Consumer<List<String>> callback) {
        return firestore.collection("salon_availabilities")
                .document(salonId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        callback.accept(new ArrayList<>());
                        return;
                    }
                    List<String> times = (List<String>) snapshot.get("times");
                    Log.d(TAG, "Frissült elérhető időpontok: " + times);
                    callback.accept(times != null ? times : new ArrayList<>());
                });
    }
}
