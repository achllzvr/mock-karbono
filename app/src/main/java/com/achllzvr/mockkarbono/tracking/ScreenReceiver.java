package com.achllzvr.mockkarbono.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.concurrent.Executors;

public class ScreenReceiver extends BroadcastReceiver {
    private static long lastScreenOnTs = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            lastScreenOnTs = System.currentTimeMillis();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            long offTs = System.currentTimeMillis();
            if (lastScreenOnTs > 0 && offTs > lastScreenOnTs) {
                long duration = offTs - lastScreenOnTs;
                lastScreenOnTs = -1;

                AppUsage usage = new AppUsage();
                usage.packageName = "SCREEN";
                usage.category = "screen";
                usage.startTimeMs = offTs - duration;
                usage.endTimeMs = offTs;
                usage.durationMs = duration;
                usage.estimatedWh = estimateWh(duration);
                usage.estimatedKgCO2 = whToKgCO2(usage.estimatedWh);
                usage.synced = false;

                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase.getInstance(context).appUsageDao().insert(usage);
                    Log.d("ScreenReceiver", "Usage saved: " + usage);
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
}