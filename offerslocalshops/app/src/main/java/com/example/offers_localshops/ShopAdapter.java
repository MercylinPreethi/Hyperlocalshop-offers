package com.example.offers_localshops;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<Product> shopList;
    private final Context context;

    public ShopAdapter(Context context, List<Product> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName, offerType, dateRange, timeRange;
        ImageView heartIcon;

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            offerType = itemView.findViewById(R.id.offerType);
            dateRange = itemView.findViewById(R.id.dateRange);
            timeRange = itemView.findViewById(R.id.timeRange);
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_product_card, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Product product = shopList.get(position);

        holder.shopName.setText(product.getShopName());
        holder.offerType.setText(product.getOfferType());

        String date = product.getFromDate() + " - " + product.getToDate();
        String time = product.getFromTime() + " - " + product.getToTime();
        holder.dateRange.setText(date);
        holder.timeRange.setText(time);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null && holder.heartIcon != null) {
            DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid).child("favorites")
                    .child("shop_" + product.getShopName().replace(" ", "_"));

            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFav = snapshot.exists();
                    holder.heartIcon.setImageResource(
                            isFav ? R.drawable.red_heart : R.drawable.heart);
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

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, productview.class);
            intent.putExtra("shopName", product.getShopName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public void updateList(List<Product> newList) {
        shopList.clear();
        shopList.addAll(newList);
        notifyDataSetChanged();
    }
}
