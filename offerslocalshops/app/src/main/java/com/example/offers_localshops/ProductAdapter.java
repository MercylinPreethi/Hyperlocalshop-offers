package com.example.offers_localshops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, offerType, offerDetail, dateRange, timeRange;
        ImageView heartIcon;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            offerType = itemView.findViewById(R.id.offerType);
            offerDetail = itemView.findViewById(R.id.offerDetail);
            dateRange = itemView.findViewById(R.id.dateRange);
            timeRange = itemView.findViewById(R.id.timeRange);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }

        public void bind(Product product) {
            productName.setText(product.getProductName());
            offerType.setText(product.getOfferType());

            if (product.getOfferDetail() != null && !product.getOfferDetail().isEmpty()) {
                offerDetail.setVisibility(View.VISIBLE);
                offerDetail.setText(product.getOfferDetail());
            } else {
                offerDetail.setVisibility(View.GONE);
            }

            dateRange.setText(product.getFromDate() + " - " + product.getToDate());
            timeRange.setText(product.getFromTime() + " - " + product.getToTime());
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        String shopName = product.getShopName();
        String key = product.getProductName();

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("favorites").child(shopName).child(key);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFav = snapshot.exists();
                holder.heartIcon.setImageResource(
                        isFav ? R.drawable.red_heart : R.drawable.heart
                );
                holder.heartIcon.setTag(isFav);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.heartIcon.setOnClickListener(v -> {
            Boolean isFav = (Boolean) holder.heartIcon.getTag();
            if (isFav == null) isFav = false;

            if (isFav) {
                favRef.removeValue();
                holder.heartIcon.setImageResource(R.drawable.heart);
                holder.heartIcon.setTag(false);
            } else {
                favRef.setValue(product);
                holder.heartIcon.setImageResource(R.drawable.red_heart);
                holder.heartIcon.setTag(true);
            }
        });
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
