package com.example.offers_localshops;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;

public class loginpage extends AppCompatActivity {

    Button vendorButton, loginButton, signupButton;
    EditText emailField, passwordField;

    private FirebaseAuth mAuth;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loginpage);

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();


        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.button4);
        emailField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);



        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(loginpage.this, SignuppageActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            loginButton.setEnabled(true);
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();

                userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(loginpage.this, "User login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(loginpage.this, HomepageActivity.class));
                            finish();
                        } else {
                            mAuth.signOut(); // Sign out non-vendor user
                            Toast.makeText(loginpage.this, "Access denied: Not a user account", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(loginpage.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
