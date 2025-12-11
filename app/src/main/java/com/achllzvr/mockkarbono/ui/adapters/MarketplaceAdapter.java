package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;

import java.util.List;

/**
 * Marketplace Adapter - Displays items in Soft Pop style cards
 */
public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.MarketViewHolder> {

    private List<MarketItem> items;
    private OnItemClickListener listener;

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
    public MarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace, parent, false);
        return new MarketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketViewHolder holder, int position) {
        MarketItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MarketViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgMarketItem;
        private TextView tvItemTitle;
        private TextView tvItemDescription;
        private TextView btnBuyItem;

        public MarketViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMarketItem = itemView.findViewById(R.id.imgMarketItem);
            tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            btnBuyItem = itemView.findViewById(R.id.btnBuyItem);
        }

        public void bind(MarketItem item) {
            tvItemTitle.setText(item.getTitle());
            tvItemDescription.setText(item.getDescription());
            btnBuyItem.setText(item.getPrice());
            imgMarketItem.setImageResource(item.getImageResId());

            btnBuyItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}

