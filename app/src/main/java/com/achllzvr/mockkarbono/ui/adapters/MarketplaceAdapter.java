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
import com.achllzvr.mockkarbono.api.data.models.MarketplaceGoal;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private List<MarketplaceGoal> goals = new ArrayList<>();
    private final OnItemClickListener listener; // Click listener interface

    // Interface for click callback
    public interface OnItemClickListener {
        void onContributeClick(MarketplaceGoal goal);
    }

    // Constructor requires listener
    public MarketplaceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setGoals(List<MarketplaceGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceGoal goal = goals.get(position);
        holder.bind(goal, listener);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvImpact;
        ImageView imgCover;
        Button btnContribute; // The button

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvImpact = itemView.findViewById(R.id.tvItemImpact);
            imgCover = itemView.findViewById(R.id.imgItemCover);
            btnContribute = itemView.findViewById(R.id.btnContribute);
        }

        public void bind(MarketplaceGoal goal, OnItemClickListener listener) {
            tvTitle.setText(goal.title);
            tvPrice.setText(String.format("₱%.0f / ₱%.0f", goal.currentAmount, goal.targetAmount));
            tvImpact.setText(goal.getProgressPercentage() + "% Funded");

            if (goal.proofImageUrl != null && !goal.proofImageUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(goal.proofImageUrl).into(imgCover);
            }

            // Bind Click
            btnContribute.setOnClickListener(v -> listener.onContributeClick(goal));
        }
    }
}