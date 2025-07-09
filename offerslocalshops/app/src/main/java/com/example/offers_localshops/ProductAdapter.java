package com.example.offers_localshops;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, offerType, offerDetail, dateRange, timeRange;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            offerType = itemView.findViewById(R.id.offerType);
            offerDetail = itemView.findViewById(R.id.offerDetail);
            dateRange = itemView.findViewById(R.id.dateRange);
            timeRange = itemView.findViewById(R.id.timeRange);
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
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
