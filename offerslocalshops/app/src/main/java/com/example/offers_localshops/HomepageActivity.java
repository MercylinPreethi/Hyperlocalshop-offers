package com.example.offers_localshops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private TextView locationText;
    private ImageView arrowIcon;
    private RecyclerView productRecyclerView;

    private Location userLocation;
    private FusedLocationProviderClient fusedClient;

    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();

    private boolean isManualLocation = false;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        locationText = findViewById(R.id.locationText);
        arrowIcon = findViewById(R.id.arrowIcon);
        productRecyclerView = findViewById(R.id.productRecyclerView);

        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(productList);
        productRecyclerView.setAdapter(adapter);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();  // Gets location and then calls fetchNearbyProducts()

        arrowIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, manualselectloc.class);
            locationPickerLauncher.launch(intent);
        });

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selected_address");
                        if (address != null && !address.isEmpty()) {
                            isManualLocation = true;
                            locationText.setText(address);
                        }
                    }
                }
        );
    }

    private void requestLocation() {
        if (isManualLocation) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && !isManualLocation) {
                userLocation = location;
                updateLocationText(location);
                fetchNearbyProducts();
            } else {
                locationText.setText("Location unavailable");
            }
        });
    }

    private void fetchNearbyProducts() {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();

                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            Double lat = vendorSnap.child("latitude").getValue(Double.class);
                            Double lng = vendorSnap.child("longitude").getValue(Double.class);

                            if (lat == null || lng == null || userLocation == null) continue;

                            float[] results = new float[1];
                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    lat, lng, results);

                            float distanceInKm = results[0] / 1000;
                            if (distanceInKm <= 90) {
                                DataSnapshot productsSnap = vendorSnap.child("products");
                                for (DataSnapshot productSnap : productsSnap.getChildren()) {
                                    Product p = productSnap.getValue(Product.class);
                                    if (p != null) {
                                        productList.add(p);
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        locationText.setText("Error loading products.");
                    }
                });
    }

    private void updateLocationText(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                locationText.setText(addresses.get(0).getAddressLine(0));
            } else {
                locationText.setText("Unable to get address");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationText.setText("Geocoder failed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            locationText.setText("Permission denied");
        }
    }
}
