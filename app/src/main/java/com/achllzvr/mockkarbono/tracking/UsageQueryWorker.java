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
import com.achllzvr.mockkarbono.utils.CarbonUtils;

import java.util.List;

public class UsageQueryWorker extends Worker {

    public UsageQueryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        long lastWindow = now - 15 * 60 * 1000; // last 15 minutes
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, lastWindow, now);
        if (stats == null) return Result.success();
        Log.d("(DEBUG) UsageQueryWorker", "Found " + stats.size() + " usage stats");

        for (UsageStats st : stats) {
            long durationMs = st.getTotalTimeInForeground();
            if (durationMs <= 0) continue;

            AppUsage usage = new AppUsage();
            usage.packageName = st.getPackageName();
            usage.category = categorizePackage(st.getPackageName());
            usage.startTimeMs = lastWindow;
            usage.endTimeMs = now;
            usage.durationMs = durationMs;
            usage.estimatedWh = CarbonUtils.wattsAndDurationToWh(5.0, durationMs); // example: 5W average
            usage.estimatedKgCO2 = CarbonUtils.whToKgCO2(usage.estimatedWh);
            usage.synced = false;

            AppDatabase.getInstance(ctx).appUsageDao().insert(usage);
            Log.d("(DEBUG) UsageQueryWorker", "Inserted usage: " + usage);
        }
        return Result.success();
    }

    private String categorizePackage(String pkg) {
        String p = pkg.toLowerCase();
        if (p.contains("youtube") || p.contains("netflix")) return "video";
        if (p.contains("facebook") || p.contains("tiktok") || p.contains("instagram")) return "social";
        if (p.contains("gmail") || p.contains("mail") || p.contains("outlook")) return "email";
        return "other";
    }
}