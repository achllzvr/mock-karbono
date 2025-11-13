package com.achllzvr.mockkarbono.ui.appusage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AppUsageAdapter extends ListAdapter<AppUsage, AppUsageAdapter.ViewHolder> {

    private final Context context;

    public AppUsageAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<AppUsage> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<AppUsage>() {
            @Override
            public boolean areItemsTheSame(@NonNull AppUsage oldItem, @NonNull AppUsage newItem) {
                return oldItem.uuid != null && oldItem.uuid.equals(newItem.uuid);
            }

            @Override
            public boolean areContentsTheSame(@NonNull AppUsage oldItem, @NonNull AppUsage newItem) {
                return oldItem.durationMs == newItem.durationMs &&
                       oldItem.packageName.equals(newItem.packageName) &&
                       oldItem.estimatedKgCO2 == newItem.estimatedKgCO2;
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_usage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAppIcon;
        private final TextView tvAppName;
        private final TextView tvDuration;
        private final TextView tvCarbonValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCarbonValue = itemView.findViewById(R.id.tvCarbonValue);
        }

        public void bind(AppUsage usage) {
            // Set app name and icon
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(usage.packageName, 0);
                tvAppName.setText(pm.getApplicationLabel(appInfo));
                ivAppIcon.setImageDrawable(pm.getApplicationIcon(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                tvAppName.setText(formatPackageName(usage.packageName));
                ivAppIcon.setImageResource(R.drawable.ic_track);
            }

            // Set duration
            tvDuration.setText(formatDuration(usage.durationMs));

            // Set carbon value
            tvCarbonValue.setText(String.format(Locale.getDefault(), "%.4f kg", usage.estimatedKgCO2));
        }

        private String formatPackageName(String packageName) {
            // Remove domain prefix (com.whatsapp → WhatsApp)
            String[] parts = packageName.split("\\.");
            if (parts.length > 0) {
                String name = parts[parts.length - 1];
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            return packageName;
        }

        private String formatDuration(long ms) {
            long hours = TimeUnit.MILLISECONDS.toHours(ms);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;

            if (hours > 0) {
                return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
            } else if (minutes > 0) {
                return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
            } else {
                return String.format(Locale.getDefault(), "%ds", seconds);
            }
        }
    }
}

