package com.achllzvr.mockkarbono.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.VH> {
    private final List<AppUsage> items = new ArrayList<>();

    public void setItems(List<AppUsage> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_usage, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AppUsage u = items.get(position);
        holder.appName.setText(u.packageName);
        holder.duration.setText(formatDuration(u.durationMs));
        holder.carbon.setText(String.format("%.4f kg", u.estimatedKgCO2));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatDuration(long ms) {
        long sec = TimeUnit.MILLISECONDS.toSeconds(ms);
        long m = sec / 60;
        long s = sec % 60;
        return m + "m " + s + "s";
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView appName, duration, carbon;
        VH(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.tvAppName);
            duration = itemView.findViewById(R.id.tvDuration);
            carbon = itemView.findViewById(R.id.tvCarbonValue);
        }
    }
}