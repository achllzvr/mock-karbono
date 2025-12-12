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

public class FriendStreakAdapter extends RecyclerView.Adapter<FriendStreakAdapter.ViewHolder> {

    private final List<FriendStreakItem> items;

    public FriendStreakAdapter(List<FriendStreakItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_streak, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;
        TextView tvStreak;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvStreak = itemView.findViewById(R.id.tvStreak);
        }

        public void bind(FriendStreakItem item) {
            tvName.setText(item.getName());
            tvStreak.setText(String.valueOf(item.getStreak()));
            
            // Use Glide for efficient image loading and caching
            Glide.with(itemView.getContext())
                .load(item.getImageResId())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(imgAvatar);
        }
    }

    public static class FriendStreakItem {
        private final String name;
        private final int streak;
        private final int imageResId;

        public FriendStreakItem(String name, int streak, int imageResId) {
            this.name = name;
            this.streak = streak;
            this.imageResId = imageResId;
        }

        public String getName() {
            return name;
        }

        public int getStreak() {
            return streak;
        }

        public int getImageResId() {
            return imageResId;
        }
    }
}
