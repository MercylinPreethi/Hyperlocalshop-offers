package com.example.offers_localshops;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomepageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private TextView locationText;
    private ImageView arrowIcon;
    private RecyclerView productRecyclerView;

    private Location userLocation;
    private FusedLocationProviderClient fusedClient;

    private ShopAdapter adapter;
    private final List<Product> productList = new ArrayList<>();

    private boolean isManualLocation = false;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    private final List<Product> fullProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        locationText = findViewById(R.id.locationText);
        arrowIcon = findViewById(R.id.arrowIcon);
        productRecyclerView = findViewById(R.id.shopRecyclerView);

        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ShopAdapter(HomepageActivity.this, productList);
        productRecyclerView.setAdapter(adapter);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        arrowIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, manualselectloc.class);
            locationPickerLauncher.launch(intent);
        });

        EditText searchBar = findViewById(R.id.searchBar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchShopByName(query);
                } else {
                    fetchNearbyProducts();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });


        setupCategoryChips();

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selected_address");
                        if (address != null && !address.isEmpty()) {
                            isManualLocation = true;
                            locationText.setText(address);

                            Geocoder geocoder = new Geocoder(this);
                            try {
                                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address addr = addresses.get(0);
                                    userLocation = new Location("manual");
                                    userLocation.setLatitude(addr.getLatitude());
                                    userLocation.setLongitude(addr.getLongitude());

                                    fetchNearbyProducts();
                                } else {
                                    locationText.setText("Location not found");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                locationText.setText("Failed to fetch coordinates");
                            }
                        }
                    }
                }
        );
    }


    private void searchShopByName(String query) {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        fullProductList.clear();

                        String lowerQuery = query.toLowerCase(Locale.getDefault());

                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            String storeName = vendorSnap.child("storeName").getValue(String.class);
                            String shopType = vendorSnap.child("shopType").getValue(String.class);
                            Double lat = vendorSnap.child("latitude").getValue(Double.class);
                            Double lng = vendorSnap.child("longitude").getValue(Double.class);

                            if (storeName == null || lat == null || lng == null) continue;
                            if (!storeName.toLowerCase().contains(lowerQuery)) continue;

                            // Distance check
                            float[] results = new float[1];
                            if (userLocation != null) {
                                Location.distanceBetween(
                                        userLocation.getLatitude(), userLocation.getLongitude(),
                                        lat, lng, results);
                                float distanceInKm = results[0] / 1000;
                                if (distanceInKm > 20) continue;
                            }

                            DataSnapshot productsSnap = vendorSnap.child("products");
                            boolean hasOffer = false;
                            Product productToShow = null;

                            for (DataSnapshot productSnap : productsSnap.getChildren()) {
                                Product p = productSnap.getValue(Product.class);
                                if (p != null) {
                                    p.setShopName(storeName);
                                    p.setShopType(shopType);
                                    productToShow = p;
                                    hasOffer = true;
                                    break;
                                }
                            }

                            if (hasOffer && productToShow != null) {
                                productList.add(productToShow);
                            } else {
                                Product noOffer = new Product();
                                noOffer.setShopName(storeName);
                                noOffer.setShopType(shopType);
                                noOffer.setOfferType("No offers available");
                                productList.add(noOffer);
                            }
                        }

                        adapter.updateList(productList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        locationText.setText("Search error.");
                    }
                });
    }



    private void requestLocation() {
        if (isManualLocation) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && !isManualLocation) {
                    userLocation = location;
                    updateLocationText(location);
                    fetchNearbyProducts();
                }
            }
        }, getMainLooper());
    }

    private void setupCategoryChips() {
        LinearLayout categoryContainer = findViewById(R.id.categoryContainer);
        String[] categories = getResources().getStringArray(R.array.shop_categories);

        for (String category : categories) {
            TextView chip = new TextView(this);
            chip.setText(category);
            chip.setBackgroundResource(R.drawable.bg_category_default);
            chip.setTextColor(getResources().getColor(R.color.black));
            chip.setPadding(32, 16, 32, 16);
            chip.setTextSize(14);
            chip.setClickable(true);
            chip.setFocusable(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 8, 12, 8);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {

                filterByCategory(category);
            });

            categoryContainer.addView(chip);
        }
    }

    private void filterByCategory(String selectedCategory) {
        if (selectedCategory.equalsIgnoreCase("All") || selectedCategory.equals("Select the Shop Type")) {
            adapter.updateList(fullProductList);
            return;
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : fullProductList) {
            if (product.getShopType() != null && product.getShopType().equalsIgnoreCase(selectedCategory)) {
                filtered.add(product);
            }
        }

        adapter.updateList(filtered);
    }


    private void fetchNearbyProducts() {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        fullProductList.clear();

                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            Double lat = vendorSnap.child("latitude").getValue(Double.class);
                            Double lng = vendorSnap.child("longitude").getValue(Double.class);
                            String storeName = vendorSnap.child("storeName").getValue(String.class);
                            String shopType = vendorSnap.child("shopType").getValue(String.class);

                            if (lat == null || lng == null || userLocation == null) continue;

                            float[] results = new float[1];
                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    lat, lng, results);

                            float distanceInKm = results[0] / 1000;
                            if (distanceInKm <= 20) {
                                DataSnapshot productsSnap = vendorSnap.child("products");
                                for (DataSnapshot productSnap : productsSnap.getChildren()) {
                                    Product p = productSnap.getValue(Product.class);
                                    if (p != null) {
                                        p.setShopName(storeName);
                                        p.setShopType(shopType);
                                        productList.add(p);
                                        fullProductList.add(p);
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
