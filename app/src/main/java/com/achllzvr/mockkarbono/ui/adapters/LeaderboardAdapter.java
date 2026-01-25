package com.achllzvr.mockkarbono.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.api.data.models.LeaderboardEntry;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardEntry> users = new ArrayList<>();

    public void setUsers(List<LeaderboardEntry> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry user = users.get(position);
        int rank = position + 1;

        holder.tvRank.setText(String.valueOf(rank));
        holder.tvUsername.setText(user.username);
        holder.tvCarbonScore.setText(String.format("%.1f kg CO₂", user.carbonSaved));
        holder.tvStreak.setText(String.valueOf(user.streak));

        // Styling for Top 3
        if (rank == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Gold
            holder.tvRank.setTextSize(20);
        } else if (rank == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Silver
            holder.tvRank.setTextSize(18);
        } else if (rank == 3) {
            holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
            holder.tvRank.setTextSize(18);
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#757575")); // Grey
            holder.tvRank.setTextSize(16);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvCarbonScore, tvStreak;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCarbonScore = itemView.findViewById(R.id.tvCarbonScore);
            tvStreak = itemView.findViewById(R.id.tvStreak);
        }
    }
}