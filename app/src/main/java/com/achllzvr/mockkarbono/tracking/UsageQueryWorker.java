package com.achllzvr.mockkarbono.tracking;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.CarbonReference;
import com.achllzvr.mockkarbono.utils.CarbonUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UsageQueryWorker extends Worker {

    private static final String TAG = "UsageQueryWorker";
    private final AppDatabase db;

    public UsageQueryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.d(TAG, "Starting Usage Query...");

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager == null) return Result.failure();

            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long startTime = calendar.getTimeInMillis();

            Map<String, UsageStats> statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);

            if (statsMap == null || statsMap.isEmpty()) {
                return Result.failure();
            }

            List<CarbonReference> references = db.carbonReferenceDao().getAll();

            for (UsageStats stats : statsMap.values()) {
                long durationMs = stats.getTotalTimeInForeground();

                if (durationMs > 1000) {
                    double durationSeconds = durationMs / 1000.0;

                    // NEW LOGIC: Use calculateCO2
                    double co2 = CarbonUtils.calculateCO2(stats.getPackageName(), (long) durationSeconds, references);

                    AppUsage entry = new AppUsage();
                    entry.uuid = UUID.randomUUID().toString();
                    entry.packageName = stats.getPackageName();
                    entry.clientCreatedAtMs = System.currentTimeMillis();
                    entry.startTimeMs = startTime;
                    entry.endTimeMs = endTime;
                    entry.durationMs = durationMs;

                    // Matches the fixed Entity
                    entry.estimatedKgCO2 = co2;

                    entry.synced = false;

                    db.appUsageDao().insert(entry);
                }
            }
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error in UsageQueryWorker", e);
            return Result.retry();
        }
    }
}