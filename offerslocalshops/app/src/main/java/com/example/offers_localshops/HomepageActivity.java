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

import com.google.android.gms.location.*;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.*;

public class HomepageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private TextView locationText;
    private ImageView arrowIcon;

    private ImageView heartIcon;
    private RecyclerView productRecyclerView;

    private Location userLocation;
    private FusedLocationProviderClient fusedClient;

    private ShopAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final List<Product> fullProductList = new ArrayList<>();

    private boolean isManualLocation = false;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        locationText = findViewById(R.id.locationText);
        arrowIcon = findViewById(R.id.arrowIcon);
        productRecyclerView = findViewById(R.id.shopRecyclerView);

        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ShopAdapter(this, productList);
        productRecyclerView.setAdapter(adapter);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation();

        ImageView heartIcon = findViewById(R.id.heartIcon);
        heartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        arrowIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, manualselectloc.class);
            locationPickerLauncher.launch(intent);
        });

        setupSearchBar();
        setupCategoryChips();
        initLocationPicker();
    }

    private void setupSearchBar() {
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchShopByName(query);
                } else {
                    fetchNearbyProducts();
                }
            }
        });
    }

    private void initLocationPicker() {
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
                                if (!addresses.isEmpty()) {
                                    Address addr = addresses.get(0);
                                    userLocation = new Location("manual");
                                    userLocation.setLatitude(addr.getLatitude());
                                    userLocation.setLongitude(addr.getLongitude());
                                    fetchNearbyProducts();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                locationText.setText("Geocode failed");
                            }
                        }
                    }
                });
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(12, 8, 12, 8);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> filterByCategory(category));
            categoryContainer.addView(chip);
        }
    }

    private void filterByCategory(String category) {
        if (category.equalsIgnoreCase("All") || category.equals("Select the Shop Type")) {
            adapter.updateList(fullProductList);
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : fullProductList) {
            if (p.getShopType() != null && p.getShopType().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    private void requestLocation() {
        if (isManualLocation) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationRequest request = LocationRequest.create();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                userLocation = locationResult.getLastLocation();
                if (userLocation != null) {
                    updateLocationText(userLocation);
                    fetchNearbyProducts();
                }
            }
        }, getMainLooper());
    }

    private void updateLocationText(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                locationText.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationText.setText("Geocoder failed");
        }
    }

    private void searchShopByName(String query) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Vendors");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onCancelled(@NonNull DatabaseError error) {}

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                fullProductList.clear();
                String lowerQuery = query.toLowerCase();

                for (DataSnapshot vendor : snapshot.getChildren()) {
                    String name = vendor.child("storeName").getValue(String.class);
                    String type = vendor.child("shopType").getValue(String.class);
                    Double lat = vendor.child("latitude").getValue(Double.class);
                    Double lng = vendor.child("longitude").getValue(Double.class);

                    if (name == null || lat == null || lng == null || !name.toLowerCase().contains(lowerQuery)) continue;
                    if (userLocation != null) {
                        float[] dist = new float[1];
                        Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), lat, lng, dist);
                        if (dist[0] / 1000 > 20) continue;
                    }

                    Product product = null;
                    for (DataSnapshot prod : vendor.child("products").getChildren()) {
                        product = prod.getValue(Product.class);
                        if (product != null) {
                            product.setShopName(name);
                            product.setShopType(type);
                            break;
                        }
                    }
                    if (product != null) productList.add(product);
                }
                adapter.updateList(productList);
            }
        });
    }

    private void fetchNearbyProducts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Vendors");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onCancelled(@NonNull DatabaseError error) {}

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                fullProductList.clear();

                for (DataSnapshot vendor : snapshot.getChildren()) {
                    Double lat = vendor.child("latitude").getValue(Double.class);
                    Double lng = vendor.child("longitude").getValue(Double.class);
                    String name = vendor.child("storeName").getValue(String.class);
                    String type = vendor.child("shopType").getValue(String.class);

                    if (lat == null || lng == null || userLocation == null) continue;

                    float[] dist = new float[1];
                    Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), lat, lng, dist);
                    if (dist[0] / 1000 > 20) continue;

                    for (DataSnapshot prodSnap : vendor.child("products").getChildren()) {
                        Product p = prodSnap.getValue(Product.class);
                        if (p != null) {
                            p.setShopName(name);
                            p.setShopType(type);
                            productList.add(p);
                            fullProductList.add(p);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            locationText.setText("Permission denied");
        }
    }
}
