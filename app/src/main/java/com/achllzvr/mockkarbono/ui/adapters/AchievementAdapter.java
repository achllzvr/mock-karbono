package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PREVIEW = 1;
    private static final int VIEW_TYPE_FULL = 2;

    private final List<Achievement> items;
    private final boolean isFullView;

    public AchievementAdapter(List<Achievement> items, boolean isFullView) {
        this.items = items;
        this.isFullView = isFullView;
    }

    @Override
    public int getItemViewType(int position) {
        return isFullView ? VIEW_TYPE_FULL : VIEW_TYPE_PREVIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_FULL) {
            View view = inflater.inflate(R.layout.item_achievement_full, parent, false);
            return new FullViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_achievement, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Achievement item = items.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_FULL) {
            ((FullViewHolder) holder).bind(item);
        } else {
            ((PreviewViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        CardView cardAchievementIcon;
        ImageView imgAchievementIcon;
        TextView tvAchievementName;

        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAchievementIcon = itemView.findViewById(R.id.cardAchievementIcon);
            imgAchievementIcon = itemView.findViewById(R.id.imgAchievementIcon);
            tvAchievementName = itemView.findViewById(R.id.tvAchievementName);
        }

        public void bind(Achievement achievement) {
            tvAchievementName.setText(achievement.getName());
            imgAchievementIcon.setImageResource(achievement.getIconResId());

            if (achievement.isUnlocked()) {
                cardAchievementIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_unlocked_bg));
                imgAchievementIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.marigold));
            } else {
                cardAchievementIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_locked_bg));
                imgAchievementIcon.setImageResource(R.drawable.ic_lock);
                imgAchievementIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_dark));
            }
        }
    }

    static class FullViewHolder extends RecyclerView.ViewHolder {
        CardView cardAchievementIcon;
        ImageView imgAchievementIcon;
        TextView tvAchievementName;
        TextView tvAchievementDesc;

        public FullViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAchievementIcon = itemView.findViewById(R.id.cardAchievementIcon);
            imgAchievementIcon = itemView.findViewById(R.id.imgAchievementIcon);
            tvAchievementName = itemView.findViewById(R.id.tvAchievementName);
            tvAchievementDesc = itemView.findViewById(R.id.tvAchievementDesc);
        }

        public void bind(Achievement achievement) {
            tvAchievementName.setText(achievement.getName());
            tvAchievementDesc.setText(achievement.getDescription());
            imgAchievementIcon.setImageResource(achievement.getIconResId());

            if (achievement.isUnlocked()) {
                cardAchievementIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_unlocked_bg));
                imgAchievementIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.marigold));
            } else {
                cardAchievementIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_locked_bg));
                imgAchievementIcon.setImageResource(R.drawable.ic_lock);
                 imgAchievementIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_dark));
            }
        }
    }

    public static class Achievement {
        private final String name;
        private final String description;
        private final int iconResId;
        private final boolean unlocked;

        public Achievement(String name, String description, int iconResId, boolean unlocked) {
            this.name = name;
            this.description = description;
            this.iconResId = iconResId;
            this.unlocked = unlocked;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getIconResId() {
            return iconResId;
        }

        public boolean isUnlocked() {
            return unlocked;
        }
    }
}
