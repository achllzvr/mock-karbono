package com.achllzvr.mockkarbono.tracking;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.concurrent.Executors;

public class AppNotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        long ts = System.currentTimeMillis();
        String category = categorizePackage(pkg);
        double kgCO2 = estimateNotificationCO2(category);
        Log.d("(DEBUG) AppNotificationListener", "Received notification: " + pkg + " - " + category + " - " + kgCO2);

        NotificationEvent evt = new NotificationEvent();
        evt.packageName = pkg;
        evt.category = category;
        evt.timestampMs = ts;
        evt.estimatedKgCO2 = kgCO2;
        evt.synced = false;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.notificationEventDao().insert(evt);
            Log.d("(DEBUG) AppNotificationListener", "Inserted notification event: " + evt);
        });
    }

    private String categorizePackage(String pkg) {
        String p = pkg.toLowerCase();
        if (p.contains("gmail") || p.contains("mail") || p.contains("outlook")) return "email";
        if (p.contains("facebook") || p.contains("instagram") || p.contains("tiktok")) return "social";
        if (p.contains("youtube") || p.contains("netflix") || p.contains("spotify")) return "media";
        return "other";
    }

    private double estimateNotificationCO2(String category) {
        switch (category) {
            case "email": return 0.004; // 4 grams
            case "social": return 0.0002; // 0.2 grams
            default: return 0.0005;
        }
    }
}