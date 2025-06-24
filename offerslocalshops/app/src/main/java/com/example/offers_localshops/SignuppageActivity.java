package com.example.offers_localshops;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignuppageActivity extends AppCompatActivity {

    private static final int USER_LAYOUT   = R.layout.user_signup_layout;
    private static final int VENDOR_LAYOUT = R.layout.vendor_signup_layout;

    private int currentLayout = USER_LAYOUT;

    private FrameLayout contentFrame;
    private Button toggleUserBtn, toggleVendorBtn, signupBtn;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuppage);

        // Init Firebase
        mAuth    = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        // Init UI
        contentFrame    = findViewById(R.id.contentFrame);
        toggleUserBtn   = findViewById(R.id.button);   // "User SignUp"
        toggleVendorBtn = findViewById(R.id.button3);  // "Vendor SignUp"
        signupBtn       = findViewById(R.id.button2);  // bottom Signup

        // Load default
        loadForm(USER_LAYOUT);

        toggleUserBtn.setOnClickListener(v -> loadForm(USER_LAYOUT));
        toggleVendorBtn.setOnClickListener(v -> loadForm(VENDOR_LAYOUT));

        signupBtn.setOnClickListener(v -> handleSignup());
    }

    /* ---------- inflate selected form into FrameLayout ---------- */
    private void loadForm(int layoutId) {
        currentLayout = layoutId;
        contentFrame.removeAllViews();
        View form = LayoutInflater.from(this).inflate(layoutId, contentFrame, false);
        contentFrame.addView(form);
    }

    /* ---------- read form fields & push to Firebase ---------- */
    private void handleSignup() {
        // find fields inside the current form
        EditText etEmail    = contentFrame.findViewById(R.id.mailid);
        EditText etPass     = contentFrame.findViewById(R.id.password);
        EditText etPhone    = contentFrame.findViewById(R.id.number);      // common phone
        String email  = textOf(etEmail);
        String pass   = textOf(etPass);
        String phone  = textOf(etPhone);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Email & password required", Toast.LENGTH_SHORT).show();
            return;
        }

        signupBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            signupBtn.setEnabled(true);
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            String uid = mAuth.getCurrentUser().getUid();
            if (currentLayout == USER_LAYOUT) {
                saveUser(uid, phone);
            } else {
                saveVendor(uid, phone);
            }
        });
    }

    private void saveUser(String uid, String phone) {
        String name = textOf((EditText) contentFrame.findViewById(R.id.username));
        DatabaseReference userRef = mRootRef.child("Users").child(uid);
        userRef.child("username").setValue(name);
        userRef.child("phone").setValue(phone);
        userRef.child("email").setValue(mAuth.getCurrentUser().getEmail());
        Toast.makeText(this, "User account created!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveVendor(String uid, String phone) {
        String store   = textOf((EditText) contentFrame.findViewById(R.id.username));
        String address = textOf((EditText) contentFrame.findViewById(R.id.location)); // field only in vendor
        DatabaseReference venRef = mRootRef.child("Vendors").child(uid);
        venRef.child("storeName").setValue(store);
        venRef.child("phone").setValue(phone);
        venRef.child("address").setValue(address);
        venRef.child("email").setValue(mAuth.getCurrentUser().getEmail());
        Toast.makeText(this, "Vendor account created!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String textOf(EditText et) { return et == null ? "" : et.getText().toString().trim(); }
}
