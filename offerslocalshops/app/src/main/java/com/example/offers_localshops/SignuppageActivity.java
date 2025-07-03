package com.example.offers_localshops;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignuppageActivity extends AppCompatActivity {

    private EditText usernameField, emailField, phoneField, passwordField;
    private Button signupBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuppage);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Bind views
        usernameField = findViewById(R.id.username);
        emailField    = findViewById(R.id.mailid);
        phoneField    = findViewById(R.id.number);
        passwordField = findViewById(R.id.password);
        signupBtn     = findViewById(R.id.button2);

        signupBtn.setOnClickListener(v -> handleSignup());
    }

    private void handleSignup() {
        String username = usernameField.getText().toString().trim();
        String email    = emailField.getText().toString().trim();
        String phone    = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        signupBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    signupBtn.setEnabled(true);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();
                    saveUserToDatabase(uid, username, email, phone);
                });
    }

    private void saveUserToDatabase(String uid, String username, String email, String phone) {
        DatabaseReference userRef = mRootRef.child("Users").child(uid);
        userRef.child("username").setValue(username);
        userRef.child("email").setValue(email);
        userRef.child("phone").setValue(phone);

        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
