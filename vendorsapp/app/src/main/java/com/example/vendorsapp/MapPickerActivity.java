package com.example.vendorsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        confirmButton = findViewById(R.id.button);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        AutocompleteSupportFragment autocompleteFragment = new AutocompleteSupportFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.autocomplete_fragment_container, autocompleteFragment)
                .commit();

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint("Search for your shop location");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLatLng() != null) {
                    selectedLatLng = place.getLatLng();
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(place.getName()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MapPickerActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        confirmButton.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLatLng.latitude);
                resultIntent.putExtra("longitude", selectedLatLng.longitude);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLatLng = new LatLng(13.0827, 80.2707); // Chennai
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 15));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("location"));
            selectedLatLng = latLng;
        });
    }
}
