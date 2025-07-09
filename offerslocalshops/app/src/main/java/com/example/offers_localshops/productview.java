package com.example.offers_localshops;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class productview extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private TextView shopTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productview);

        String shopName = getIntent().getStringExtra("shopName");

        recyclerView = findViewById(R.id.productRecyclerView);
        shopTitle = findViewById(R.id.shopTitle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        ImageView backArrow = findViewById(R.id.arrowIcon);
        backArrow.setOnClickListener(v -> finish());

        if (shopName != null) {
            shopTitle.setText(shopName);
            loadProductsForShop(shopName);
        } else {
            Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductsForShop(String shopName) {
        DatabaseReference vendorsRef = FirebaseDatabase.getInstance().getReference("Vendors");
        vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                    String store = vendorSnap.child("storeName").getValue(String.class); // or shopName
                    if (store != null && store.equalsIgnoreCase(shopName)) {
                        productList.clear(); // 💡 clear old data
                        DataSnapshot productsSnap = vendorSnap.child("products");
                        for (DataSnapshot productSnap : productsSnap.getChildren()) {
                            Product p = productSnap.getValue(Product.class);
                            if (p != null) {
                                p.setShopName(store);
                                productList.add(p);
                            }
                        }
                        break; // stop looping after match
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(productview.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }
}