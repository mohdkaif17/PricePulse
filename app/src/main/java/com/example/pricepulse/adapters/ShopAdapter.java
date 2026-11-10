package com.example.pricepulse.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.R;
import com.example.pricepulse.models.Shop;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final Context context;
    private final List<Shop> shopList;
    private final OnShopClickListener listener;

    public interface OnShopClickListener {
        void onShopClick(Shop shop);
    }

    public ShopAdapter(Context context, List<Shop> shopList, OnShopClickListener listener) {
        this.context = context;
        this.shopList = shopList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_card, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.bind(shop, listener);
    }

    @Override
    public int getItemCount() {
        return shopList != null ? shopList.size() : 0;
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtShopName;
        private final TextView txtShopCategory;
        private final TextView txtShopDistance;
        private final TextView txtShopDescription;
        private final TextView txtShopRating;
        private final ImageView imgShopIcon;
        private final View btnViewShop;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtShopCategory = itemView.findViewById(R.id.txtShopCategory);
            txtShopDistance = itemView.findViewById(R.id.txtShopDistance);
            txtShopDescription = itemView.findViewById(R.id.txtShopDescription);
            txtShopRating = itemView.findViewById(R.id.txtShopRating);
            imgShopIcon = itemView.findViewById(R.id.imgShopIcon);
            btnViewShop = itemView.findViewById(R.id.btnViewShop);
        }

        public void bind(Shop shop, OnShopClickListener listener) {
            txtShopName.setText(shop.getName());
            txtShopCategory.setText(shop.getCategory());
            txtShopDistance.setText(shop.getDistance());
            txtShopDescription.setText(shop.getDescription());
            txtShopRating.setText(shop.getRating());

            imgShopIcon.setImageResource(shop.getIconResId());
            imgShopIcon.setBackgroundTintList(ColorStateList.valueOf(shop.getBadgeBgColor()));

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onShopClick(shop));
                if (btnViewShop != null) {
                    btnViewShop.setOnClickListener(v -> listener.onShopClick(shop));
                }
            }
        }
    }
}
