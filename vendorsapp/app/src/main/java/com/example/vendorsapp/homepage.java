package com.example.vendorsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class homepage extends AppCompatActivity {

    private EditText weekdayOpening, weekdayClosing, weekendOpening, weekendClosing, customShopTypeField;;
    private FirebaseAuth mAuth;
    private DatabaseReference vendorRef;

    private Spinner shopTypeSpinner;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        vendorRef = FirebaseDatabase.getInstance().getReference("Vendors");

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔍 Check if shop type and timings already exist
        vendorRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                boolean hasShopType = task.getResult().child("shopType").exists();
                boolean hasTimings = task.getResult().child("timings").child("weekday").child("open").exists()
                        && task.getResult().child("timings").child("weekday").child("close").exists()
                        && task.getResult().child("timings").child("weekend").child("open").exists()
                        && task.getResult().child("timings").child("weekend").child("close").exists();

                if (hasShopType && hasTimings) {
                    Intent intent = new Intent(homepage.this, secondpage.class); // Replace with actual class
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    initUI();
                }
            } else {
                Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void saveShopTimingsAndGoNext() {
        String wdOpen = weekdayOpening.getText().toString().trim();
        String wdClose = weekdayClosing.getText().toString().trim();
        String weOpen = weekendOpening.getText().toString().trim();
        String weClose = weekendClosing.getText().toString().trim();

        String shopType = shopTypeSpinner.getSelectedItem().toString();
        if (shopType.equalsIgnoreCase("Other")) {
            shopType = customShopTypeField.getText().toString().trim();
        }

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference vendorDataRef = vendorRef.child(uid);

        vendorDataRef.child("timings").child("weekday").child("open").setValue(wdOpen);
        vendorDataRef.child("timings").child("weekday").child("close").setValue(wdClose);
        vendorDataRef.child("timings").child("weekend").child("open").setValue(weOpen);
        vendorDataRef.child("timings").child("weekend").child("close").setValue(weClose);

        vendorDataRef.child("shopType").setValue(shopType).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(homepage.this, secondpage.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save shop type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        String wdOpen = weekdayOpening.getText().toString().trim();
        String wdClose = weekdayClosing.getText().toString().trim();
        String weOpen = weekendOpening.getText().toString().trim();
        String weClose = weekendClosing.getText().toString().trim();
        String shopType = shopTypeSpinner.getSelectedItem().toString();
        String customType = customShopTypeField.getText().toString().trim();

        if (TextUtils.isEmpty(wdOpen) || TextUtils.isEmpty(wdClose)
                || TextUtils.isEmpty(weOpen) || TextUtils.isEmpty(weClose)) {
            return false;
        }

        if (shopType.equalsIgnoreCase("Other") && TextUtils.isEmpty(customType)) {
            return false;
        }

        return true;
    }

    private void initUI() {
        shopTypeSpinner     = findViewById(R.id.shopType);
        customShopTypeField = findViewById(R.id.customShopType);
        weekdayOpening      = findViewById(R.id.weekdayOpening);
        weekdayClosing      = findViewById(R.id.weekdayClosing);
        weekendOpening      = findViewById(R.id.weekendOpening);
        weekendClosing      = findViewById(R.id.weekendClosing);
        button              = findViewById(R.id.saveTimingBtn); // ✅ FIXED

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.shop_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shopTypeSpinner.setAdapter(adapter);

        shopTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = shopTypeSpinner.getSelectedItem().toString();
                if (selected.equalsIgnoreCase("Other")) {
                    customShopTypeField.setVisibility(View.VISIBLE);
                } else {
                    customShopTypeField.setVisibility(View.GONE);
                    customShopTypeField.setText("");
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                customShopTypeField.setVisibility(View.GONE);
            }
        });

        button.setOnClickListener(v -> {
            if (validateInputs()) {
                saveShopTimingsAndGoNext();  // ✅ Only save when valid
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });

        // ❌ REMOVE THIS:
        // saveShopTimingsAndGoNext();
    }
}
