package com.example.inkturnejava;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inkturnejava.auth.AuthManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private FirebaseAuth auth;
    private Spinner styleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager();
        auth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText nameEditText = findViewById(R.id.nameEditText);
        Switch isSalonSwitch = findViewById(R.id.isSalonSwitch);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        styleSpinner = findViewById(R.id.styleSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tattoo_styles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        styleSpinner.setAdapter(adapter);
        styleSpinner.setVisibility(View.GONE);

        isSalonSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            styleSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!email.isEmpty() && password.length() >= 6) {
                authManager.loginUser(email, password, (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(this, "Hiba: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Töltsd ki az emailt és a jelszót!", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String role = isSalonSwitch.isChecked() ? "tattoo_salon" : "user";
            String style = isSalonSwitch.isChecked() ? styleSpinner.getSelectedItem().toString() : null;

            if (!email.isEmpty() && password.length() >= 6 && !name.isEmpty()) {
                authManager.registerUser(email, password, role, name, style, (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(this, "Hiba: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Tölts ki minden mezőt helyesen!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}