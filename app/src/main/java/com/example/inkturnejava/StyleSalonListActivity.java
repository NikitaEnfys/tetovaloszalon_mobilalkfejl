package com.example.inkturnejava;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StyleSalonListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private SalonAdapter adapter;
    private FirebaseFirestore firestore;
    private List<Salon> allSalons = new ArrayList<>();
    private List<String> docIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);

        recyclerView = findViewById(R.id.salonRecyclerView);
        searchInput = findViewById(R.id.searchEditText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        firestore = FirebaseFirestore.getInstance();

        String style = getIntent().getStringExtra("style");
        if (style == null) {
            Toast.makeText(this, "Hiányzó stílus paraméter", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore.collection("tattoo_salons")
                .whereEqualTo("style", style)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allSalons.clear();
                    docIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allSalons.add(doc.toObject(Salon.class));
                        docIds.add(doc.getId());
                    }
                    adapter = new SalonAdapter(allSalons, docIds);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}