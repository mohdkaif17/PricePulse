package com.example.pricepulse.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.R;
import com.example.pricepulse.models.CategoryProduct;
import com.example.pricepulse.utils.PriceStatusHelper;

import java.util.List;

public class ShopProductAdapter extends RecyclerView.Adapter<ShopProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<CategoryProduct> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(CategoryProduct product);
    }

    public ShopProductAdapter(Context context, List<CategoryProduct> productList) {
        this(context, productList, null);
    }

    public ShopProductAdapter(Context context, List<CategoryProduct> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_product_card, parent, false);
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
        private final TextView txtSub;
        private final TextView txtPrice;
        private final TextView txtStatus;
        private final View layoutStatus;
        private final View dotStatus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtShopProductName);
            txtSub = itemView.findViewById(R.id.txtShopProductSub);
            txtPrice = itemView.findViewById(R.id.txtShopProductPrice);
            txtStatus = itemView.findViewById(R.id.txtShopProductStatus);
            layoutStatus = itemView.findViewById(R.id.layoutShopProductStatus);
            dotStatus = itemView.findViewById(R.id.dotShopProductStatus);
        }

        public void bind(Context context, CategoryProduct product, OnProductClickListener listener) {
            txtName.setText(product.getName());
            txtSub.setText(product.getCategory());
            txtPrice.setText(product.getPrice());
            txtStatus.setText(product.getPriceStatus());

            PriceStatusHelper.applyStatusPill(context, layoutStatus, txtStatus, null, dotStatus, product.getPriceStatus());

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onProductClick(product));
            }
        }
    }
}
