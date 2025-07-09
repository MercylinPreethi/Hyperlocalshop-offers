package com.example.offers_localshops;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shopName);
            offerType = itemView.findViewById(R.id.offerType);
            dateRange = itemView.findViewById(R.id.dateRange);
            timeRange = itemView.findViewById(R.id.timeRange);
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
