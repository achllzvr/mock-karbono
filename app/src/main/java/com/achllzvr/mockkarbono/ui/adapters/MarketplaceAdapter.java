package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.data.models.MarketplaceGoal;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private List<MarketplaceGoal> goals = new ArrayList<>();

    public void setGoals(List<MarketplaceGoal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We reuse the existing layout but bind different data
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marketplace_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceGoal goal = goals.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvImpact;
        // ImageView imgCover; // Uncomment if you use Glide

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvImpact = itemView.findViewById(R.id.tvItemImpact);
            // imgCover = itemView.findViewById(R.id.imgItemCover);
        }

        public void bind(MarketplaceGoal goal) {
            tvTitle.setText(goal.title);

            // Format: "₱5,000 / ₱10,000"
            String progressText = String.format("₱%.0f / ₱%.0f", goal.currentAmount, goal.targetAmount);
            tvPrice.setText(progressText);

            // Show Percentage as "Impact"
            tvImpact.setText(goal.getProgressPercentage() + "% Funded");
        }
    }
}