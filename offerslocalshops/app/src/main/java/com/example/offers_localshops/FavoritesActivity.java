package com.example.offers_localshops;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private final List<Product> favoriteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(favoriteList);
        recyclerView.setAdapter(adapter);

        loadFavoriteProducts();
    }

    private void loadFavoriteProducts() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("favorites");

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product p = snap.getValue(Product.class);
                    if (p != null) {
                        favoriteList.add(p);
                    }
                }

                adapter.notifyDataSetChanged();
                if (favoriteList.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "No favorites yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritesActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
