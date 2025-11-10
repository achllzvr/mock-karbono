package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.tracking.SyncWorker;
import com.achllzvr.mockkarbono.ui.views.CircularProgressView;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView tvTodayTotal;
    private TextView tvSafeRange;
    private TextView tvStreakValue;
    private TextView tvUnsyncedBadge;
    private TextView tvMetricPhoneValue;
    private TextView tvMetricAppliancesValue;
    private TextView tvMetricNotificationsValue;
    private Button btnForceSync;
    private CircularProgressView progressRing;

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        tvTodayTotal = view.findViewById(R.id.tvTodayTotal);
        tvSafeRange = view.findViewById(R.id.tvSafeRange);
        tvStreakValue = view.findViewById(R.id.tvStreakValue);
        tvUnsyncedBadge = view.findViewById(R.id.tvUnsyncedBadge);
        tvMetricPhoneValue = view.findViewById(R.id.tvMetricPhoneValue);
        tvMetricAppliancesValue = view.findViewById(R.id.tvMetricAppliancesValue);
        tvMetricNotificationsValue = view.findViewById(R.id.tvMetricNotificationsValue);
        btnForceSync = view.findViewById(R.id.btnForceSync);
        progressRing = view.findViewById(R.id.progressRing);

        // Set up force sync button
        btnForceSync.setOnClickListener(v -> forceSync());

        // Load data
        loadDashboardData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String today = getTodayDate();

            // Get today's app usage
            List<AppUsage> todayUsage = db.appUsageDao().getLatest(100);
            double phoneCO2 = 0.0;
            for (AppUsage usage : todayUsage) {
                if (isSameDay(usage.clientCreatedAtMs, System.currentTimeMillis())) {
                    phoneCO2 += usage.estimatedKgCO2;
                }
            }

            // Get today's notifications
            List<NotificationEvent> todayNotifs = db.notificationEventDao().getUnsynced();
            double notifCO2 = 0.0;
            for (NotificationEvent notif : todayNotifs) {
                if (isSameDay(notif.clientCreatedAtMs, System.currentTimeMillis())) {
                    notifCO2 += notif.estimatedKgCO2;
                }
            }

            // Get appliances
            List<ApplianceLog> appliances = db.applianceDao().getAll();
            double applianceCO2 = 0.0;
            for (ApplianceLog app : appliances) {
                applianceCO2 += app.estimatedKgCO2PerDay;
            }

            // Total CO2
            double totalCO2 = phoneCO2 + notifCO2 + applianceCO2;

            // Unsynced count
            int unsyncedCount = db.appUsageDao().countUnsynced() +
                               db.notificationEventDao().countUnsynced() +
                               db.applianceDao().countUnsynced();

            // Calculate streak (simplified - days under safe range)
            int streak = calculateStreak();

            // Safe range check (0.9 kg/day)
            double safeRange = 0.9;
            boolean withinSafeRange = totalCO2 <= safeRange;
            float progress = (float) (totalCO2 / safeRange);

            // Update UI on main thread
            double finalPhoneCO2 = phoneCO2;
            double finalNotifCO2 = notifCO2;
            double finalApplianceCO2 = applianceCO2;
            double finalTotalCO2 = totalCO2;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTodayTotal.setText(String.format(Locale.US, "%.2f kg CO₂", finalTotalCO2));
                    tvMetricPhoneValue.setText(String.format(Locale.US, "%.2f kg", finalPhoneCO2));
                    tvMetricAppliancesValue.setText(String.format(Locale.US, "%.2f kg", finalApplianceCO2));
                    tvMetricNotificationsValue.setText(String.format(Locale.US, "%.3f kg", finalNotifCO2));

                    if (withinSafeRange) {
                        tvSafeRange.setText("✓ Within safe range");
                        tvSafeRange.setTextColor(getResources().getColor(R.color.colorAccent, null));
                    } else {
                        tvSafeRange.setText("⚠ Above safe range");
                        tvSafeRange.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
                    }

                    tvStreakValue.setText(String.format(Locale.US, "%d days", streak));

                    if (unsyncedCount > 0) {
                        tvUnsyncedBadge.setVisibility(View.VISIBLE);
                        tvUnsyncedBadge.setText(String.valueOf(unsyncedCount));
                    } else {
                        tvUnsyncedBadge.setVisibility(View.GONE);
                    }

                    if (progressRing != null) {
                        progressRing.setProgress(Math.min(progress, 1.0f));
                    }
                });
            }
        });
    }

    private void forceSync() {
        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(requireContext()).enqueue(syncWork);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                android.widget.Toast.makeText(requireContext(), "Sync started...", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        // Reload after 2 seconds
        new android.os.Handler().postDelayed(this::loadDashboardData, 2000);
    }

    private int calculateStreak() {
        // Simplified streak calculation
        // TODO: Implement proper streak logic with DailySummary table
        return 4; // Placeholder
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(Calendar.getInstance().getTime());
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}

