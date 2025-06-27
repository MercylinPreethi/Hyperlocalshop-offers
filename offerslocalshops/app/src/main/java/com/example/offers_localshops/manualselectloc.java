package com.example.offers_localshops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class manualselectloc extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private FusedLocationProviderClient fusedClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manualselectloc);

        ImageView backArrow = findViewById(R.id.arrowIcon);
        backArrow.setOnClickListener(v -> finish());

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // ✅ Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // ✅ Autocomplete Fragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            ));
            autocompleteFragment.setHint("Search your area or landmark");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    String address = place.getAddress();
                    double lat = place.getLatLng().latitude;
                    double lng = place.getLatLng().longitude;

                    sendLocationBack(address, lat, lng);
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(manualselectloc.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ✅ Use current location button
        LinearLayout currentLocationLayout = findViewById(R.id.currentLocationLayout);
        TextView currentLocationText = findViewById(R.id.currentLocationText);

        currentLocationLayout.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                return;
            }

            fusedClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (!addresses.isEmpty()) {
                            String address = addresses.get(0).getAddressLine(0);

                            // ✅ Update text on the button-like layout
                            currentLocationText.setText("Use Current Location\n" + address);

                            // Optional: send back result after clicking again
                            currentLocationLayout.setOnClickListener(inner -> {
                                sendLocationBack(address, location.getLatitude(), location.getLongitude());
                            });
                        } else {
                            currentLocationText.setText("Unable to get address");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        currentLocationText.setText("Geocoder failed");
                    }
                } else {
                    currentLocationText.setText("Location unavailable");
                }
            });
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        String address = addresses.get(0).getAddressLine(0);
                        sendLocationBack(address, location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Geocoder error", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLocationBack(String address, double lat, double lng) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_address", address);
        resultIntent.putExtra("lat", lat);
        resultIntent.putExtra("lng", lng);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
