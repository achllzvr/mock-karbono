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
import com.achllzvr.mockkarbono.ui.views.DayProgressCircle;

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
    private TextView tvPhoneCarbon;
    private TextView tvAppliancesCarbon;
    private TextView tvNotificationsCarbon;
    private Button btnForceSync;
    private CircularProgressView progressRing;

    // Weekly progress circles
    private DayProgressCircle[] dayCircles = new DayProgressCircle[7];
    private TextView[] dayLabels = new TextView[7];
    private String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

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
        tvPhoneCarbon = view.findViewById(R.id.tvPhoneCarbon);
        tvAppliancesCarbon = view.findViewById(R.id.tvAppliancesCarbon);
        tvNotificationsCarbon = view.findViewById(R.id.tvNotificationsCarbon);
        btnForceSync = view.findViewById(R.id.btnForceSync);
        progressRing = view.findViewById(R.id.progressRing);

        // Bind weekly progress circles
        dayCircles[0] = view.findViewById(R.id.dayCircle1);
        dayCircles[1] = view.findViewById(R.id.dayCircle2);
        dayCircles[2] = view.findViewById(R.id.dayCircle3);
        dayCircles[3] = view.findViewById(R.id.dayCircle4);
        dayCircles[4] = view.findViewById(R.id.dayCircle5);
        dayCircles[5] = view.findViewById(R.id.dayCircle6);
        dayCircles[6] = view.findViewById(R.id.dayCircle7);

        // Bind day label TextViews
        dayLabels[0] = view.findViewById(R.id.tvDay1Label);
        dayLabels[1] = view.findViewById(R.id.tvDay2Label);
        dayLabels[2] = view.findViewById(R.id.tvDay3Label);
        dayLabels[3] = view.findViewById(R.id.tvDay4Label);
        dayLabels[4] = view.findViewById(R.id.tvDay5Label);
        dayLabels[5] = view.findViewById(R.id.tvDay6Label);
        dayLabels[6] = view.findViewById(R.id.tvDay7Label);

        // Set date numbers for the week and mark today
        setupWeeklyDates();

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
            List<NotificationEvent> todayNotifs = db.notificationEventDao().getAll();
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

            // Calculate streak
            int streak = calculateStreak();

            // Safe range check (6.8 kg/day)
            double safeRange = 6.8;
            boolean withinSafeRange = totalCO2 <= safeRange;
            float progress = (float) (totalCO2 / safeRange);

            // Make final for lambda
            double finalPhoneCO2 = phoneCO2;
            double finalNotifCO2 = notifCO2;
            double finalApplianceCO2 = applianceCO2;
            double finalTotalCO2 = totalCO2;
            int finalUnsyncedCount = unsyncedCount;
            int finalStreak = streak;

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTodayTotal.setText(String.format(Locale.US, "%.2f", finalTotalCO2));
                    tvPhoneCarbon.setText(String.format(Locale.US, "%.2f", finalPhoneCO2));
                    tvAppliancesCarbon.setText(String.format(Locale.US, "%.2f", finalApplianceCO2));
                    tvNotificationsCarbon.setText(String.format(Locale.US, "%.2f", finalNotifCO2));

                    if (withinSafeRange) {
                        tvSafeRange.setText("✓ Within safe range");
                        tvSafeRange.setTextColor(getResources().getColor(R.color.colorAccent, null));
                    } else {
                        tvSafeRange.setText("⚠ Above safe range");
                        tvSafeRange.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
                    }

                    tvStreakValue.setText(String.valueOf(finalStreak));

                    if (finalUnsyncedCount > 0) {
                        tvUnsyncedBadge.setVisibility(View.VISIBLE);
                        tvUnsyncedBadge.setText(String.valueOf(finalUnsyncedCount));
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

    private void setupWeeklyDates() {
        Calendar today = Calendar.getInstance();
        int todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2, ..., Saturday = 7

        // Calculate the start of the week (Sunday)
        Calendar weekStart = Calendar.getInstance();
        int daysFromSunday = todayDayOfWeek - Calendar.SUNDAY; // 0 if today is Sunday
        weekStart.add(Calendar.DAY_OF_MONTH, -daysFromSunday);

        // Set date numbers for each day circle (Sunday-Saturday)
        for (int i = 0; i < 7; i++) {
            int dayOfMonth = weekStart.get(Calendar.DAY_OF_MONTH);
            dayCircles[i].setDateText(String.valueOf(dayOfMonth));

            // Check if this is today
            if (i == daysFromSunday) {
                dayCircles[i].setToday(true);
                // Change label to "Today" and style it
                dayLabels[i].setText("Today");
                dayLabels[i].setTextColor(getResources().getColor(R.color.colorAccent, null));
                dayLabels[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                dayCircles[i].setToday(false);
                // Set normal day label
                dayLabels[i].setText(dayNames[i]);
                dayLabels[i].setTextColor(getResources().getColor(R.color.colorCarbonGray, null));
                dayLabels[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            weekStart.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Load progress data for the week in background
        loadWeeklyProgressData();
    }

    private void loadWeeklyProgressData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Calendar weekStart = Calendar.getInstance();
            int todayDayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
            int daysFromSunday = todayDayOfWeek - Calendar.SUNDAY;
            weekStart.add(Calendar.DAY_OF_MONTH, -daysFromSunday);

            // Safe range per day
            double safeRange = 6.8;

            // Get data for each day of the week
            for (int i = 0; i < 7; i++) {
                String dateStr = getDateString(weekStart);
                long dateStartMs = weekStart.getTimeInMillis();
                long dateEndMs = dateStartMs + (24 * 60 * 60 * 1000); // End of day

                // Calculate CO2 for this day
                double dayCO2 = 0.0;

                // Get app usage for this day
                List<AppUsage> dayUsage = db.appUsageDao().getLatest(1000);
                for (AppUsage usage : dayUsage) {
                    if (usage.clientCreatedAtMs >= dateStartMs && usage.clientCreatedAtMs < dateEndMs) {
                        dayCO2 += usage.estimatedKgCO2;
                    }
                }

                // Get notifications for this day (get all, not just unsynced)
                List<NotificationEvent> allNotifs = db.notificationEventDao().getAll();
                for (NotificationEvent notif : allNotifs) {
                    if (notif.clientCreatedAtMs >= dateStartMs && notif.clientCreatedAtMs < dateEndMs) {
                        dayCO2 += notif.estimatedKgCO2;
                    }
                }

                // Get appliances for all days (they contribute to each day)
                List<ApplianceLog> appliances = db.applianceDao().getAll();
                for (ApplianceLog app : appliances) {
                    dayCO2 += app.estimatedKgCO2PerDay;
                }

                // Calculate progress (0.0 to 1.0+)
                float progress = (float) (dayCO2 / safeRange);
                boolean isComplete = dayCO2 > 0 && dayCO2 <= safeRange;

                // Make final for lambda
                final int circleIndex = i;
                final float finalProgress = progress;
                final boolean finalIsComplete = isComplete;
                final double finalDayCO2 = dayCO2;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (circleIndex == daysFromSunday) {
                            // Today - show progress
                            dayCircles[circleIndex].setProgress(Math.min(finalProgress, 1.0f));
                        } else if (circleIndex < daysFromSunday) {
                            // Past days - show complete or exceeded
                            if (finalIsComplete) {
                                dayCircles[circleIndex].setComplete(true);
                            } else if (finalDayCO2 > 0) {
                                dayCircles[circleIndex].setProgress(1.0f); // Show full red ring if exceeded
                            }
                        }
                        // Future days remain empty (gray outline)
                    });
                }

                weekStart.add(Calendar.DAY_OF_MONTH, 1);
            }
        });
    }

    private String getDateString(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(calendar.getTime());
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
        // Calculate streak by checking consecutive days within safe range
        double safeRange = 6.8; // kg CO2 per day
        int streak = 0;

        Calendar calendar = Calendar.getInstance();
        // Start from yesterday and go backwards
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // Check up to 30 days back
        for (int i = 0; i < 30; i++) {
            long dayStartMs = getDayStartTimestamp(calendar);
            long dayEndMs = dayStartMs + (24 * 60 * 60 * 1000);

            double dayCO2 = calculateDayCO2(dayStartMs, dayEndMs);

            // If within safe range, increment streak
            if (dayCO2 > 0 && dayCO2 <= safeRange) {
                streak++;
            } else if (dayCO2 > 0) {
                // Day has data but exceeded safe range - streak broken
                break;
            }
            // If dayCO2 == 0, no data for this day, continue checking older days

            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }

    private long getDayStartTimestamp(Calendar calendar) {
        Calendar dayStart = (Calendar) calendar.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        return dayStart.getTimeInMillis();
    }

    private double calculateDayCO2(long dayStartMs, long dayEndMs) {
        double dayCO2 = 0.0;

        try {
            // Get app usage for this day
            List<AppUsage> dayUsage = db.appUsageDao().getLatest(1000);
            for (AppUsage usage : dayUsage) {
                if (usage.clientCreatedAtMs >= dayStartMs && usage.clientCreatedAtMs < dayEndMs) {
                    dayCO2 += usage.estimatedKgCO2;
                }
            }

            // Get notifications for this day
            List<NotificationEvent> dayNotifs = db.notificationEventDao().getAll();
            for (NotificationEvent notif : dayNotifs) {
                if (notif.clientCreatedAtMs >= dayStartMs && notif.clientCreatedAtMs < dayEndMs) {
                    dayCO2 += notif.estimatedKgCO2;
                }
            }

            // Get appliances (they contribute to every day)
            List<ApplianceLog> appliances = db.applianceDao().getAll();
            for (ApplianceLog app : appliances) {
                dayCO2 += app.estimatedKgCO2PerDay;
            }
        } catch (Exception e) {
            // If database error, return 0
            e.printStackTrace();
        }

        return dayCO2;
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

