package com.achllzvr.mockkarbono.tracking;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.utils.CarbonUtils;

import java.util.List;
import java.util.UUID;

public class UsageQueryWorker extends Worker {

    public UsageQueryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("(DEBUG) UsageQueryWorker", "UsageQueryWorker.doWork start");
        Context ctx = getApplicationContext();
        UsageStatsManager usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager pm = ctx.getPackageManager(); // Needed for detailed category detection

        long now = System.currentTimeMillis();
        long lastWindow = now - 15 * 60 * 1000; // last 15 minutes

        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, lastWindow, now);
        if (stats == null) return Result.success();

        AppDatabase db = AppDatabase.getInstance(ctx);

        for (UsageStats st : stats) {
            long durationMs = st.getTotalTimeInForeground();
            if (durationMs <= 0) continue;

            // Duplicate Check (Last 20 mins)
            long twentyMinutesAgo = now - 20 * 60 * 1000;
            AppUsage recentUsage = db.appUsageDao().getRecentByPackage(st.getPackageName(), twentyMinutesAgo);

            if (recentUsage != null) {
                continue; // Skip duplicate
            }

            String pkgName = st.getPackageName();
            String category = determineCategory(pm, pkgName);

            AppUsage usage = new AppUsage();
            usage.uuid = UUID.randomUUID().toString();
            usage.packageName = pkgName;
            usage.category = category;
            usage.startTimeMs = lastWindow;
            usage.endTimeMs = now;
            usage.durationMs = durationMs;

            // 1. Calculate TRUE Carbon Cost (Cloud + Device)
            usage.estimatedKgCO2 = CarbonUtils.calculateAppEmissions(category, durationMs);

            // 2. Store Device Energy (Wh) for reference/database consistency
            // We use standard 5W here to represent the battery drain portion, distinct from the total CO2 footprint
            usage.estimatedWh = CarbonUtils.wattsAndDurationToWh(CarbonUtils.AVG_DEVICE_WATTS, durationMs);

            usage.clientCreatedAtMs = System.currentTimeMillis();
            usage.synced = false;

            db.appUsageDao().insert(usage);
            Log.d("(DEBUG) UsageQueryWorker", "Inserted: " + pkgName + " [" + category + "] CO2: " + usage.estimatedKgCO2);
        }
        return Result.success();
    }

    private String determineCategory(PackageManager pm, String pkg) {
        String p = pkg.toLowerCase();

        // 1. Explicit Overrides (High Priority)
        if (p.contains("youtube") || p.contains("netflix") || p.contains("twitch") || p.contains("disney")) return "video";
        if (p.contains("facebook") || p.contains("tiktok") || p.contains("instagram") || p.contains("twitter") || p.contains("discord") || p.contains("telegram") || p.contains("messenger")) return "social";
        if (p.contains("gmail") || p.contains("mail") || p.contains("outlook")) return "email";

        // 2. Android System Categories (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (info.category == ApplicationInfo.CATEGORY_GAME) return "game";
                if (info.category == ApplicationInfo.CATEGORY_SOCIAL) return "social";
                if (info.category == ApplicationInfo.CATEGORY_VIDEO) return "video";
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found locally
            }
        }

        // 3. Fallback Keyword Detection for Games
        if (p.contains("game") || p.contains("clash") || p.contains("roblox") || p.contains("genshin") || p.contains("legends") || p.contains("minecraft")) return "game";

        return "other";
    }
}