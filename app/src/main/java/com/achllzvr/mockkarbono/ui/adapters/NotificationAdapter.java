package com.achllzvr.mockkarbono.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationEvent> items = new ArrayList<>();

    public void setItems(List<NotificationEvent> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationEvent notif = items.get(position);

        // Format package name (remove domain prefix)
        String displayName = formatPackageName(notif.packageName);
        holder.tvAppName.setText(displayName);

        // Show category
        holder.tvCategory.setText(notif.category);

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        String timeStr = sdf.format(new Date(notif.timestampMs));
        holder.tvTimestamp.setText(timeStr);

        // Show CO2 estimate
        holder.tvCO2Estimate.setText(String.format(Locale.US, "%.4f kg CO₂", notif.estimatedKgCO2));

        // Sync status
        if (notif.synced) {
            holder.tvSyncStatus.setText("✓");
            holder.tvSyncStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.colorAccent, null));
        } else {
            holder.tvSyncStatus.setText("○");
            holder.tvSyncStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.colorCarbonGray, null));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatPackageName(String packageName) {
        if (packageName == null) return "Unknown";

        // Extract app name from package (e.g., com.facebook.katana -> Facebook)
        if (packageName.contains("facebook")) return "Facebook";
        if (packageName.contains("instagram")) return "Instagram";
        if (packageName.contains("whatsapp")) return "WhatsApp";
        if (packageName.contains("gmail")) return "Gmail";
        if (packageName.contains("youtube")) return "YouTube";
        if (packageName.contains("tiktok")) return "TikTok";
        if (packageName.contains("twitter") || packageName.contains("x.")) return "X (Twitter)";
        if (packageName.contains("telegram")) return "Telegram";
        if (packageName.contains("snapchat")) return "Snapchat";
        if (packageName.contains("spotify")) return "Spotify";

        // Generic fallback - show last part
        String[] parts = packageName.split("\\.");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            return lastPart.substring(0, 1).toUpperCase() + lastPart.substring(1);
        }

        return packageName;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        TextView tvCategory;
        TextView tvTimestamp;
        TextView tvCO2Estimate;
        TextView tvSyncStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppName = itemView.findViewById(R.id.tvNotifAppName);
            tvCategory = itemView.findViewById(R.id.tvNotifCategory);
            tvTimestamp = itemView.findViewById(R.id.tvNotifTimestamp);
            tvCO2Estimate = itemView.findViewById(R.id.tvNotifCO2);
            tvSyncStatus = itemView.findViewById(R.id.tvNotifSyncStatus);
        }
    }
}

