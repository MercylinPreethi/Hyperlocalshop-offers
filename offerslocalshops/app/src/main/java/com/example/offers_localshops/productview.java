package com.example.offers_localshops;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class productview extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
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

        setupScrollableDates();

        if (shopName != null) {
            shopTitle.setText(shopName);
            loadProductsForShop(shopName);
        } else {
            Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupScrollableDates() {
        LinearLayout dateContainer = findViewById(R.id.dateContainer);
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
        SimpleDateFormat compareFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String displayDate = displayFormat.format(calendar.getTime());
            String compareDate = compareFormat.format(calendar.getTime());

            TextView dateChip = new TextView(this);
            dateChip.setText(displayDate);
            dateChip.setTag(compareDate);
            dateChip.setPadding(40, 20, 40, 20);
            dateChip.setTextSize(16);
            dateChip.setTextColor(getResources().getColor(R.color.black));
            dateChip.setBackgroundResource(R.drawable.bg_category_default);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 8, 16, 8);
            dateChip.setLayoutParams(params);

            dateChip.setOnClickListener(v -> {
                String selectedDateStr = (String) v.getTag();
                filterProductsByDate(selectedDateStr);
            });

            dateContainer.addView(dateChip);
            calendar.add(Calendar.DATE, 1);
        }
    }

    private void filterProductsByDate(String selectedDateStr) {
        List<Product> filteredList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date selectedDate = format.parse(selectedDateStr);

            for (Product p : allProducts) {
                if (p.getFromDate() != null && p.getToDate() != null) {
                    Date fromDate = format.parse(p.getFromDate());
                    Date toDate = format.parse(p.getToDate());

                    if (fromDate != null && toDate != null &&
                            !selectedDate.before(fromDate) && !selectedDate.after(toDate)) {
                        filteredList.add(p);
                    }
                }
            }

            adapter.updateList(filteredList);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadProductsForShop(String shopName) {
        DatabaseReference vendorsRef = FirebaseDatabase.getInstance().getReference("Vendors");
        vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                    String store = vendorSnap.child("storeName").getValue(String.class);
                    if (store != null && store.equalsIgnoreCase(shopName)) {
                        productList.clear();
                        allProducts.clear();
                        DataSnapshot productsSnap = vendorSnap.child("products");
                        for (DataSnapshot productSnap : productsSnap.getChildren()) {
                            Product p = productSnap.getValue(Product.class);
                            if (p != null) {
                                p.setShopName(store);
                                productList.add(p);
                                allProducts.add(p);
                            }
                        }
                        break;
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
