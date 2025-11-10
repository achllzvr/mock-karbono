package com.achllzvr.mockkarbono.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.tracking.AppNotificationListener;
import com.achllzvr.mockkarbono.tracking.SyncWorker;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SyncSettingsFragment extends Fragment {

    private TextView tvLastSynced;
    private SwitchMaterial switchAutoSync;
    private Button btnSyncNow;
    private TextView tvSafeRangeValue;
    private SwitchMaterial switchDailyReminders;
    private TextView tvNotificationListenerStatus;
    private Button btnEnableNotificationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_settings, container, false);

        // Bind views
        tvLastSynced = view.findViewById(R.id.tvLastSynced);
        switchAutoSync = view.findViewById(R.id.switchAutoSync);
        btnSyncNow = view.findViewById(R.id.btnSyncNow);
        tvSafeRangeValue = view.findViewById(R.id.tvSafeRangeValue);
        switchDailyReminders = view.findViewById(R.id.switchDailyReminders);
        tvNotificationListenerStatus = view.findViewById(R.id.tvNotificationListenerStatus);
        btnEnableNotificationListener = view.findViewById(R.id.btnEnableNotificationListener);

        // Setup listeners
        setupSyncControls();
        setupNotificationListenerStatus();

        // Load saved preferences
        loadPreferences();

        return view;
    }

    private void setupSyncControls() {
        btnSyncNow.setOnClickListener(v -> {
            syncNow();
        });

        switchAutoSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("auto_sync", isChecked)
                    .apply();

            android.widget.Toast.makeText(requireContext(),
                isChecked ? "Auto-sync enabled" : "Auto-sync disabled",
                android.widget.Toast.LENGTH_SHORT).show();
        });

        switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("daily_reminders", isChecked)
                    .apply();

            android.widget.Toast.makeText(requireContext(),
                isChecked ? "Daily reminders enabled" : "Daily reminders disabled",
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNotificationListenerStatus() {
        if (isNotificationServiceEnabled()) {
            tvNotificationListenerStatus.setText("✓ Notification tracking active");
            tvNotificationListenerStatus.setTextColor(getResources().getColor(R.color.colorAccent, null));
            btnEnableNotificationListener.setVisibility(View.GONE);
        } else {
            tvNotificationListenerStatus.setText("⚠ Notification tracking disabled");
            tvNotificationListenerStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
            btnEnableNotificationListener.setVisibility(View.VISIBLE);

            btnEnableNotificationListener.setOnClickListener(v -> {
                showNotificationListenerDialog();
            });
        }
    }

    private void syncNow() {
        btnSyncNow.setEnabled(false);
        btnSyncNow.setText("Syncing...");

        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(requireContext()).enqueue(syncWork);

        android.widget.Toast.makeText(requireContext(), "Sync started...", android.widget.Toast.LENGTH_SHORT).show();

        // Update last synced time
        new android.os.Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateLastSyncedTime();
                    btnSyncNow.setEnabled(true);
                    btnSyncNow.setText("Sync Now");
                });
            }
        }, 3000);
    }

    private void updateLastSyncedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        String timeStr = sdf.format(new Date());
        tvLastSynced.setText("Last synced: " + timeStr);

        // Save to preferences
        requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_sync_time", System.currentTimeMillis())
                .apply();
    }

    private void loadPreferences() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("karbono_prefs", Context.MODE_PRIVATE);

        // Load auto-sync
        boolean autoSync = prefs.getBoolean("auto_sync", true);
        switchAutoSync.setChecked(autoSync);

        // Load daily reminders
        boolean dailyReminders = prefs.getBoolean("daily_reminders", false);
        switchDailyReminders.setChecked(dailyReminders);

        // Load last sync time
        long lastSyncTime = prefs.getLong("last_sync_time", 0);
        if (lastSyncTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            String timeStr = sdf.format(new Date(lastSyncTime));
            tvLastSynced.setText("Last synced: " + timeStr);
        } else {
            tvLastSynced.setText("Never synced");
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = requireContext().getPackageName();
        final String flat = Settings.Secure.getString(requireContext().getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showNotificationListenerDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Enable Notification Tracking")
                .setMessage("Karbono needs Notification Access to track notifications and estimate their carbon footprint.\n\nPlease enable 'Karbono' in the next screen.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupNotificationListenerStatus();
    }
}

