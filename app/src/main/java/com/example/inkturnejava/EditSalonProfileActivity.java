package com.example.inkturnejava;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSalonProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    private ImageView profileImageView;
    private EditText editName, editDescription, editAddress, editEmail, editPhone, editHours;
    private Button saveButton, addGalleryImageButton;
    private RecyclerView galleryRecyclerView;

    private Uri selectedProfileImageUri = null;
    private List<Uri> selectedGalleryUris = new ArrayList<>();
    private List<String> uploadedGalleryUrls = new ArrayList<>();
    private GalleryAdapter galleryAdapter;

    private String salonId;

    private final ActivityResultLauncher<Intent> profileImagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedProfileImageUri = result.getData().getData();
                    profileImageView.setImageURI(selectedProfileImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryImagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedGalleryUris.add(imageUri);
                        galleryAdapter.addImage(imageUri.toString());
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_salon_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        salonId = auth.getCurrentUser().getUid();

        profileImageView = findViewById(R.id.profileImageView);
        editName = findViewById(R.id.editName);
        editDescription = findViewById(R.id.editDescription);
        editAddress = findViewById(R.id.editAddress);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editHours = findViewById(R.id.editHours);
        saveButton = findViewById(R.id.saveButton);
        addGalleryImageButton = findViewById(R.id.addGalleryImageButton);
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);

        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        galleryAdapter = new GalleryAdapter(this, new ArrayList<>());
        galleryRecyclerView.setAdapter(galleryAdapter);

        loadCurrentProfile();

        profileImageView.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            profileImagePickerLauncher.launch(pickIntent);
        });

        addGalleryImageButton.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryImagePickerLauncher.launch(pickIntent);
        });

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        firestore.collection("tattoo_salons")
                .document(salonId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editName.setText(doc.getString("name"));
                        editDescription.setText(doc.getString("description"));
                        editAddress.setText(doc.getString("address"));
                        editEmail.setText(doc.getString("email"));
                        editPhone.setText(doc.getString("phone"));
                        editHours.setText(doc.getString("openingHours"));

                        String profileUrl = doc.getString("profileImageUrl");
                        if (profileUrl != null) {
                            Glide.with(this).load(profileUrl).into(profileImageView);
                        }

                        List<String> galleryUrls = (List<String>) doc.get("galleryImageUrls");
                        if (galleryUrls != null) {
                            uploadedGalleryUrls.addAll(galleryUrls);
                            galleryAdapter.setImageUrls(galleryUrls);
                        }
                    }
                });
    }

    private void saveProfile() {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", editName.getText().toString());
        updatedData.put("description", editDescription.getText().toString());
        updatedData.put("address", editAddress.getText().toString());
        updatedData.put("email", editEmail.getText().toString());
        updatedData.put("phone", editPhone.getText().toString());
        updatedData.put("openingHours", editHours.getText().toString());

        if (selectedProfileImageUri != null) {
            StorageReference profileRef = storageRef.child("salon_profiles/" + salonId + "/profile.jpg");
            profileRef.putFile(selectedProfileImageUri)
                    .continueWithTask(task -> profileRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> {
                        updatedData.put("profileImageUrl", uri.toString());
                        uploadGalleryImages(updatedData);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a profilkép feltöltésekor.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            uploadGalleryImages(updatedData);
        }
    }

    private void uploadGalleryImages(Map<String, Object> updatedData) {
        if (selectedGalleryUris.isEmpty()) {
            updatedData.put("galleryImageUrls", uploadedGalleryUrls);
            updateFirestore(updatedData);
            return;
        }

        List<String> newGalleryUrls = new ArrayList<>(uploadedGalleryUrls);
        final int[] uploadsRemaining = {selectedGalleryUris.size()};

        for (Uri uri : selectedGalleryUris) {
            StorageReference galleryRef = storageRef.child("salon_profiles/" + salonId + "/gallery_" + System.currentTimeMillis() + ".jpg");
            galleryRef.putFile(uri)
                    .continueWithTask(task -> galleryRef.getDownloadUrl())
                    .addOnSuccessListener(downloadUri -> {
                        newGalleryUrls.add(downloadUri.toString());
                        uploadsRemaining[0]--;
                        if (uploadsRemaining[0] == 0) {
                            updatedData.put("galleryImageUrls", newGalleryUrls);
                            updateFirestore(updatedData);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a galériakép feltöltésénél.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFirestore(Map<String, Object> updatedData) {
        firestore.collection("tattoo_salons")
                .document(salonId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil frissítve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a mentés során.", Toast.LENGTH_SHORT).show();
                });
    }
}
