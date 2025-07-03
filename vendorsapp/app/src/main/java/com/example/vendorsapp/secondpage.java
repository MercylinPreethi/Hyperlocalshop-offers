package com.example.vendorsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.Calendar;
import java.util.Locale;

public class secondpage extends AppCompatActivity {

    private LinearLayout productContainer;
    private Button addProductButton;
    private LayoutInflater inflater;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_secondpage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productContainer = findViewById(R.id.productContainer);
        addProductButton = findViewById(R.id.addProductButton);
        inflater = LayoutInflater.from(this);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference vendorRef = FirebaseDatabase.getInstance().getReference("Vendors");
        Button saveBtn = findViewById(R.id.saveBtn);




        saveBtn.setOnClickListener(v -> {
            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            if (uid == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference productsRef = vendorRef.child(uid).child("products");
            productsRef.removeValue(); // optional: clears previous offers

            for (int i = 0; i < productContainer.getChildCount(); i++) {
                View row = productContainer.getChildAt(i);

                EditText productName = row.findViewById(R.id.productName);
                Spinner offerTypeSpinner = row.findViewById(R.id.offerTypeSpinner);
                EditText offerDetail = row.findViewById(R.id.offerDetail);
                EditText fromDate = row.findViewById(R.id.fromDate);
                EditText toDate = row.findViewById(R.id.toDate);
                EditText fromTime = row.findViewById(R.id.fromTime);
                EditText toTime = row.findViewById(R.id.toTime);



                String name = productName.getText().toString().trim();
                String offerType = offerTypeSpinner.getSelectedItem().toString();
                String offerDetailText = offerDetail.getText().toString().trim();

                String fromDateVal = fromDate.getText().toString().trim();
                String toDateVal = toDate.getText().toString().trim();
                String fromTimeVal = fromTime.getText().toString().trim();
                String toTimeVal = toTime.getText().toString().trim();

                if (name.isEmpty() || fromDateVal.isEmpty() || toDateVal.isEmpty() || fromTimeVal.isEmpty() || toTimeVal.isEmpty()) {
                    Toast.makeText(this, "Please fill all product details", Toast.LENGTH_SHORT).show();
                    return;
                }

                String offerDetailValue;
                if (offerType.equalsIgnoreCase("Discount") || offerType.equalsIgnoreCase("Cashback") || offerType.equalsIgnoreCase("Other")) {
                    offerDetailValue = offerDetailText;
                } else {
                    offerDetailValue = offerType;
                }

                // Push this product into Firebase
                DatabaseReference rowRef = productsRef.push();
                rowRef.child("productName").setValue(name);
                rowRef.child("offerType").setValue(offerType);
                rowRef.child("offerDetail").setValue(offerDetailValue);
                rowRef.child("fromDate").setValue(fromDateVal);
                rowRef.child("toDate").setValue(toDateVal);
                rowRef.child("fromTime").setValue(fromTimeVal);
                rowRef.child("toTime").setValue(toTimeVal);
            }

            Toast.makeText(this, "All offers saved!", Toast.LENGTH_SHORT).show();
        });

        addNewProductRow();

        addProductButton.setOnClickListener(v -> addNewProductRow());
    }

    private void addNewProductRow() {
        View rowView = inflater.inflate(R.layout.offer_row, productContainer, false);

        Spinner offerTypeSpinner = rowView.findViewById(R.id.offerTypeSpinner);
        EditText offerDetail = rowView.findViewById(R.id.offerDetail);
        EditText fromDate = rowView.findViewById(R.id.fromDate);
        EditText toDate = rowView.findViewById(R.id.toDate);
        EditText fromTime = rowView.findViewById(R.id.fromTime);
        EditText toTime = rowView.findViewById(R.id.toTime);
        ImageButton removeBtn = rowView.findViewById(R.id.removeProductBtn);

        offerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = offerTypeSpinner.getSelectedItem().toString();

                if (selected.equalsIgnoreCase("Discount") || selected.equalsIgnoreCase("Cashback") || selected.equalsIgnoreCase("Other")) {
                    offerDetail.setVisibility(View.VISIBLE);
                    offerDetail.setHint("Enter " + selected.toLowerCase() + " details");
                } else {
                    offerDetail.setVisibility(View.GONE);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        removeBtn.setOnClickListener(v -> {
            productContainer.removeView(rowView);
        });

        View.OnClickListener datePicker = v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(secondpage.this, (view, y, m, d) ->
                    ((EditText) v).setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day).show();
        };

        fromDate.setOnClickListener(v -> showDatePicker(fromDate));
        toDate.setOnClickListener(v -> showDatePicker(toDate));
        fromTime.setOnClickListener(v -> showTimePicker(fromTime));
        toTime.setOnClickListener(v -> showTimePicker(toTime));

        productContainer.addView(rowView);
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            target.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            target.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }
}
