package com.example.inkturnejava;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class SalonDetailsActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private ViewPager2 galleryViewPager;
    private TextView descriptionText, addressText, emailText, phoneText, hoursText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_details);

        profileImageView = findViewById(R.id.profileImageView);
        galleryViewPager = findViewById(R.id.galleryViewPager);
        descriptionText = findViewById(R.id.descriptionText);
        addressText = findViewById(R.id.addressText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        hoursText = findViewById(R.id.hoursText);

        String salonId = getIntent().getStringExtra("salonId");

        FirebaseFirestore.getInstance()
                .collection("tattoo_salons")
                .document(salonId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String profileUrl = doc.getString("profileImageUrl");
                        List<String> galleryUrls = (List<String>) doc.get("galleryImageUrls");
                        descriptionText.setText(doc.getString("description"));
                        addressText.setText("Cím: " + doc.getString("address"));
                        emailText.setText("Email: " + doc.getString("email"));
                        phoneText.setText("Telefon: " + doc.getString("phone"));
                        hoursText.setText("Nyitvatartás: " + doc.getString("hours"));

                        Glide.with(this).load(profileUrl).into(profileImageView);

                        if (galleryUrls != null) {
                            galleryViewPager.setAdapter(new GalleryAdapter(this, galleryUrls));
                        }
                    }
                });
    }
}
