package com.example.pricepulse.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.R;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ManageListingsAdapter extends RecyclerView.Adapter<ManageListingsAdapter.ListingViewHolder> {

    public interface OnListingActionListener {
        void onEdit(ApiModels.ListingModel listing);
        void onDelete(ApiModels.ListingModel listing);
        void onModerate(ApiModels.ListingModel listing);
        void onReport(ApiModels.ListingModel listing);
    }

    private List<ApiModels.ListingModel> listings = new ArrayList<>();
    private final boolean isAdmin;
    private final OnListingActionListener listener;

    public ManageListingsAdapter(Context context, OnListingActionListener listener) {
        this.isAdmin = ApiClient.isAdmin();
        this.listener = listener;
    }

    public ManageListingsAdapter(boolean isAdmin, OnListingActionListener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setListings(List<ApiModels.ListingModel> listings) {
        this.listings = listings != null ? listings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_listing, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        ApiModels.ListingModel item = listings.get(position);

        String title = item.customTitle != null && !item.customTitle.isEmpty()
                ? item.customTitle : "Listing #" + item.id;
        holder.txtTitle.setText(title);

        String providerName = item.providerName != null && !item.providerName.isEmpty()
                ? item.providerName : "Direct Offering";
        String areaName = item.areaName != null && !item.areaName.isEmpty()
                ? item.areaName : "Bengaluru";
        holder.txtProviderAndArea.setText(providerName + " • " + areaName);

        String categoryName = item.categoryName != null ? item.categoryName : "";
        String subcategoryName = item.subcategoryName != null ? item.subcategoryName : "";
        if (!categoryName.isEmpty() && !subcategoryName.isEmpty()) {
            holder.txtCategoryTag.setText(categoryName + " • " + subcategoryName);
        } else if (!categoryName.isEmpty()) {
            holder.txtCategoryTag.setText(categoryName);
        } else {
            holder.txtCategoryTag.setText("General");
        }

        if (Boolean.TRUE.equals(item.isPriceAvailable) && item.priceAmount != null) {
            holder.txtPrice.setText("₹" + String.format("%,.0f", item.priceAmount));
        } else {
            holder.txtPrice.setText("Quote on Request");
        }

        String priceTypeName = item.priceTypeName != null && !item.priceTypeName.isEmpty()
                ? item.priceTypeName : "Selling Price";
        holder.txtPriceTypeName.setText(priceTypeName);

        String status = item.status != null ? item.status.toLowerCase() : "active";
        if ("active".equals(status)) {
            holder.txtModerationBadge.setText("Active");
            holder.txtModerationBadge.setTextColor(Color.parseColor("#065F46"));
            holder.txtModerationBadge.setBackgroundResource(R.drawable.bg_status_best_deal);
        } else if ("pending".equals(status)) {
            holder.txtModerationBadge.setText("Pending Review");
            holder.txtModerationBadge.setTextColor(Color.parseColor("#92400E"));
            holder.txtModerationBadge.setBackgroundResource(R.drawable.bg_status_fair);
        } else if ("archived".equals(status)) {
            holder.txtModerationBadge.setText("Archived");
            holder.txtModerationBadge.setTextColor(Color.parseColor("#4B5563"));
            holder.txtModerationBadge.setBackgroundResource(R.drawable.bg_stat_card_gray);
        } else if ("suspended".equals(status)) {
            holder.txtModerationBadge.setText("Suspended");
            holder.txtModerationBadge.setTextColor(Color.parseColor("#991B1B"));
            holder.txtModerationBadge.setBackgroundResource(R.drawable.bg_status_overpriced);
        } else {
            holder.txtModerationBadge.setText("Rejected");
            holder.txtModerationBadge.setTextColor(Color.parseColor("#991B1B"));
            holder.txtModerationBadge.setBackgroundResource(R.drawable.bg_status_overpriced);
        }

        if (item.rejectionReason != null && !item.rejectionReason.trim().isEmpty()) {
            holder.txtRejectionNotice.setVisibility(View.VISIBLE);
            holder.txtRejectionNotice.setText("Notice: " + item.rejectionReason);
        } else {
            holder.txtRejectionNotice.setVisibility(View.GONE);
        }

        holder.btnModerate.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });

        holder.btnModerate.setOnClickListener(v -> {
            if (listener != null) listener.onModerate(item);
        });

        holder.btnReportListing.setOnClickListener(v -> {
            if (listener != null) listener.onReport(item);
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtModerationBadge;
        TextView txtProviderAndArea;
        TextView txtCategoryTag;
        TextView txtPrice;
        TextView txtPriceTypeName;
        TextView txtRejectionNotice;
        MaterialButton btnEdit;
        MaterialButton btnDelete;
        MaterialButton btnModerate;
        MaterialButton btnReportListing;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtListingTitle);
            txtModerationBadge = itemView.findViewById(R.id.txtModerationBadge);
            txtProviderAndArea = itemView.findViewById(R.id.txtListingProviderAndArea);
            txtCategoryTag = itemView.findViewById(R.id.txtListingCategoryTag);
            txtPrice = itemView.findViewById(R.id.txtListingPrice);
            txtPriceTypeName = itemView.findViewById(R.id.txtPriceTypeName);
            txtRejectionNotice = itemView.findViewById(R.id.txtRejectionNotice);
            btnEdit = itemView.findViewById(R.id.btnEditListing);
            btnDelete = itemView.findViewById(R.id.btnDeleteListing);
            btnModerate = itemView.findViewById(R.id.btnModerateListing);
            btnReportListing = itemView.findViewById(R.id.btnReportListing);
        }
    }
}
