package com.example.vendorsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signuppage extends AppCompatActivity {

    private EditText usernameField, mailField, phoneField, locationField;
    private TextInputEditText passwordField;
    private Button signupBtn, pickLocationBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference vendorRef;

    private double selectedLat = 0.0, selectedLng = 0.0;
    private static final int MAP_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signuppage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainFrame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        vendorRef = FirebaseDatabase.getInstance().getReference("Vendors");

        usernameField   = findViewById(R.id.username);
        passwordField   = findViewById(R.id.password);
        mailField       = findViewById(R.id.mailid);
        phoneField      = findViewById(R.id.number);
        locationField   = findViewById(R.id.location);
        signupBtn       = findViewById(R.id.button2);
        pickLocationBtn = findViewById(R.id.pickLocationBtn);

        signupBtn.setOnClickListener(v -> performSignup());

        pickLocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(signuppage.this, MapPickerActivity.class);
            startActivityForResult(intent, MAP_REQUEST);
        });
    }

    private void performSignup() {
        String name     = usernameField.getText().toString().trim();
        String email    = mailField.getText().toString().trim();
        String pass     = passwordField.getText().toString().trim();
        String phone    = phoneField.getText().toString().trim();
        String location = locationField.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Name, Email, and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        signupBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            signupBtn.setEnabled(true);
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                DatabaseReference ref = vendorRef.child(uid);

                ref.child("storeName").setValue(name);
                ref.child("email").setValue(email);
                ref.child("password").setValue(pass);
                ref.child("phone").setValue(phone);
                ref.child("latitude").setValue(selectedLat);
                ref.child("longitude").setValue(selectedLng);

                Toast.makeText(this, "Vendor registered successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String textOf(EditText et) { return et == null ? "" : et.getText().toString().trim(); }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("latitude", 0.0);
            selectedLng = data.getDoubleExtra("longitude", 0.0);
            String address = data.getStringExtra("address");

            if (locationField != null) {
                locationField.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
            }
        }
    }
}
