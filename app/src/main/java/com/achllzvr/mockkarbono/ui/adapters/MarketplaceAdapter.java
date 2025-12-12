package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;

import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private List<MarketItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MarketItem item);
    }

    public MarketplaceAdapter(List<MarketItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<MarketItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ CRITICAL: Ensure this points to your NEW GRID layout file
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Renamed to standard 'ViewHolder' to match the class definition
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvLocation;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Match these IDs with your 'item_marketplace_grid.xml'
            imgItem = itemView.findViewById(R.id.imgMarketItem);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDesc = itemView.findViewById(R.id.tvItemDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnAction = itemView.findViewById(R.id.btnAction); // Ensure XML has this ID, not 'btnBuyItem'
        }

        public void bind(final MarketItem item, final OnItemClickListener listener) {
            // Null checks to prevent crashes if a view is missing in XML
            if (tvTitle != null) tvTitle.setText(item.getTitle());
            if (tvDesc != null) tvDesc.setText(item.getDescription());
            if (imgItem != null) imgItem.setImageResource(item.getImageResId());

            if (btnAction != null) {
                btnAction.setText(item.getPrice());
                btnAction.setOnClickListener(v -> listener.onItemClick(item));
            }

            if (tvLocation != null) {
                // Simple logic for demo location
                if (item.getTitle().contains("Palawan")) tvLocation.setText("Palawan");
                else if (item.getTitle().contains("Cebu")) tvLocation.setText("Cebu");
                else if (item.getTitle().contains("Mindanao")) tvLocation.setText("Davao");
                else tvLocation.setText("PH");
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}