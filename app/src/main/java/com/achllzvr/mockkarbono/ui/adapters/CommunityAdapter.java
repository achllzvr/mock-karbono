package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    private List<Community> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Community item);
    }

    public CommunityAdapter(List<Community> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<Community> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_groups, parent, false);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCommunity;
        TextView tvTitle;
        TextView tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Correctly bind to the views in item_community_groups.xml
            imgCommunity = itemView.findViewById(R.id.imgCommunity);
            tvTitle = itemView.findViewById(R.id.tvCommunityName);
            tvDesc = itemView.findViewById(R.id.tvCommunityMemberCount);
        }

        public void bind(final Community item, final OnItemClickListener listener) {
            if (tvTitle != null) tvTitle.setText(item.getTitle());
            if (tvDesc != null) tvDesc.setText(item.getDescription());
            
            if (imgCommunity != null) {
                Glide.with(itemView.getContext())
                    .load(item.getImageResId())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imgCommunity);
            }
            
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
