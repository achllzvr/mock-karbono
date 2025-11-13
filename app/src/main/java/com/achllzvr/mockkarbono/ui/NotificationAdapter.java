package com.achllzvr.mockkarbono.ui.notifications;

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
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter extends ListAdapter<NotificationEvent, NotificationAdapter.ViewHolder> {

    private final Context context;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public NotificationAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<NotificationEvent> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<NotificationEvent>() {
            @Override
            public boolean areItemsTheSame(@NonNull NotificationEvent oldItem,
                                         @NonNull NotificationEvent newItem) {
                return oldItem.uuid != null && oldItem.uuid.equals(newItem.uuid);
            }

            @Override
            public boolean areContentsTheSame(@NonNull NotificationEvent oldItem,
                                            @NonNull NotificationEvent newItem) {
                return oldItem.timestampMs == newItem.timestampMs &&
                       oldItem.packageName.equals(newItem.packageName);
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAppIcon;
        private final TextView tvAppName;
        private final TextView tvTimestamp;
        private final TextView tvCarbonValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvCarbonValue = itemView.findViewById(R.id.tvCarbonValue);
        }

        public void bind(NotificationEvent notification) {
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(notification.packageName, 0);
                tvAppName.setText(pm.getApplicationLabel(appInfo));
                ivAppIcon.setImageDrawable(pm.getApplicationIcon(appInfo));
            } catch (PackageManager.NameNotFoundException e) {
                tvAppName.setText(notification.packageName);
                ivAppIcon.setImageResource(R.drawable.ic_notification);
            }

            tvTimestamp.setText(timeFormat.format(new Date(notification.timestampMs)));
            tvCarbonValue.setText(String.format(Locale.getDefault(), "%.4f kg",
                                notification.estimatedKgCO2));
        }
    }
}