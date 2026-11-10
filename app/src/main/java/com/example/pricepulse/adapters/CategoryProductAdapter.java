package com.example.pricepulse.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.R;
import com.example.pricepulse.models.CategoryProduct;
import com.example.pricepulse.utils.PriceStatusHelper;

import java.util.List;

public class CategoryProductAdapter extends RecyclerView.Adapter<CategoryProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<CategoryProduct> productList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CategoryProduct product);
    }

    public CategoryProductAdapter(Context context, List<CategoryProduct> productList) {
        this(context, productList, null);
    }

    public CategoryProductAdapter(Context context, List<CategoryProduct> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        CategoryProduct product = productList.get(position);
        holder.bind(context, product, listener);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final TextView txtCategory;
        private final TextView txtPrice;
        private final TextView txtStatus;
        private final TextView txtMarketAvg;
        private final ImageView imgIcon;
        private final ImageView imgStatusBadge;
        private final View layoutStatus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtCategory = itemView.findViewById(R.id.txtProductDesc);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtStatus = itemView.findViewById(R.id.txtProductStatus);
            txtMarketAvg = itemView.findViewById(R.id.txtProductMarketAvg);
            imgIcon = itemView.findViewById(R.id.imgProductIcon);
            imgStatusBadge = itemView.findViewById(R.id.imgProductStatusBadge);
            layoutStatus = itemView.findViewById(R.id.layoutProductStatus);
        }

        public void bind(Context context, CategoryProduct product, OnItemClickListener listener) {
            txtName.setText(product.getName());
            txtCategory.setText(product.getCategory());
            txtPrice.setText(product.getPrice());
            txtStatus.setText(product.getPriceStatus());
            txtMarketAvg.setText(product.getMarketAvg());

            imgIcon.setImageResource(product.getIconResId());
            imgIcon.setBackgroundTintList(ColorStateList.valueOf(product.getBadgeBgColor()));

            PriceStatusHelper.applyStatusPill(context, layoutStatus, txtStatus, imgStatusBadge, null, product.getPriceStatus());

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(product));
            }
        }
    }
}
