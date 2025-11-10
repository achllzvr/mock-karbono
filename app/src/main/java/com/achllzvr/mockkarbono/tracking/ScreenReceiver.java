package com.achllzvr.mockkarbono.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.UUID;
import java.util.concurrent.Executors;

public class ScreenReceiver extends BroadcastReceiver {
    private static long lastScreenOnTs = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("KarbonoDebug", "ScreenReceiver.onReceive action=" + action);

        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            lastScreenOnTs = System.currentTimeMillis();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            long offTs = System.currentTimeMillis();
            if (lastScreenOnTs > 0 && offTs > lastScreenOnTs) {
                long duration = offTs - lastScreenOnTs;
                lastScreenOnTs = -1;

                AppUsage usage = new AppUsage();
                usage.uuid = UUID.randomUUID().toString();
                usage.packageName = "SCREEN";
                usage.category = "screen";
                usage.startTimeMs = offTs - duration;
                usage.endTimeMs = offTs;
                usage.durationMs = duration;
                usage.estimatedWh = estimateWh(duration);
                usage.estimatedKgCO2 = whToKgCO2(usage.estimatedWh);
                usage.clientCreatedAtMs = System.currentTimeMillis();
                usage.synced = false;

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        // log DB path
                        String dbPath = context.getDatabasePath("karbono.db").getAbsolutePath();
                        Log.d("KarbonoDebug", "ScreenReceiver DB path=" + dbPath);
                        long id = AppDatabase.getInstance(context).appUsageDao().insert(usage);
                        Log.d("KarbonoDebug", "Inserted SCREEN session id=" + id + " durationMs=" + duration);
                        int unsynced = AppDatabase.getInstance(context).appUsageDao().countUnsynced();
                        Log.d("KarbonoDebug", "AppUsage unsynced count after insert=" + unsynced);
                    } catch (Exception ex) {
                        Log.e("KarbonoDebug", "DB insert failed for SCREEN session", ex);
                    }
                });
            }
        }
    }

    private double estimateWh(long durationMs) {
        double hours = durationMs / 1000.0 / 3600.0;
        double activeW = 5.0; // average active power
        return activeW * hours;
    }

    private double whToKgCO2(double wh) {
        double gridKgPerKwh = 0.8; // fallback
        return (wh / 1000.0) * gridKgPerKwh;
    }

    public static IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        return filter;
    }
}