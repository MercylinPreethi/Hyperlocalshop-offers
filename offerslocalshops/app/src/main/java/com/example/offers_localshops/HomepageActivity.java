package com.example.offers_localshops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;



public class HomepageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private ActivityResultLauncher<Intent> locationPickerLauncher;

    private TextView locationText;

    private FusedLocationProviderClient fusedClient;

    private ImageView arrowicon;

    private boolean isManualLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        locationText = findViewById(R.id.locationText);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocation();

        TextView locationText = findViewById(R.id.locationText);
        ImageView arrow = findViewById(R.id.arrowIcon);

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selected_address");
                        isManualLocation = true;
                        locationText.setText(address);
                    }
                }
        );

        arrow.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, manualselectloc.class);
            locationPickerLauncher.launch(intent);
        });

    }

    private void requestLocation() {
        if (isManualLocation) return; // ❌ Skip if manual location already selected

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && !isManualLocation) {  // ✅ Check again
                updateLocationText(location);
            } else if (!isManualLocation) {
                locationText.setText("Location unavailable");
            }
        });

        }

    private void updateLocationText(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String fullAddress = addresses.get(0).getAddressLine(0);
                locationText.setText(fullAddress);
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
            requestLocation(); // Retry if permission granted
        } else {
            locationText.setText("Permission denied");
        }
    }
}

