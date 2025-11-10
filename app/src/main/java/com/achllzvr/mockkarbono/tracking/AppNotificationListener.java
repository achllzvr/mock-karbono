package com.achllzvr.mockkarbono.tracking;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppNotificationListener extends NotificationListenerService {
    private static final String TAG = "(DEBUG) AppNotificationListener";
    // single-thread executor for DB writes from this service
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "NotificationListener onListenerConnected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "NotificationListener onListenerDisconnected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        Log.d(TAG, "onNotificationPosted pkg=" + pkg);

        // Build event
        final NotificationEvent evt = new NotificationEvent();
        evt.uuid = UUID.randomUUID().toString();
        evt.packageName = pkg;
        evt.category = categorizePackage(pkg);
        evt.timestampMs = System.currentTimeMillis();
        evt.estimatedKgCO2 = estimateNotificationCO2(evt.category);
        evt.clientCreatedAtMs = System.currentTimeMillis();
        evt.synced = false;

        // Insert on background thread to avoid Room's main-thread restriction
        dbExecutor.execute(() -> {
            try {
                long id = AppDatabase.getInstance(getApplicationContext())
                        .notificationEventDao()
                        .insert(evt);
                Log.d(TAG, "Inserted NotificationEvent id=" + id + " uuid=" + evt.uuid);
                int unsynced = AppDatabase.getInstance(getApplicationContext())
                        .notificationEventDao()
                        .countUnsynced();
                Log.d(TAG, "Notification unsynced count after insert=" + unsynced);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting NotificationEvent", e);
            }
        });
    }

    private String categorizePackage(String pkg) {
        String p = pkg == null ? "" : pkg.toLowerCase();
        if (p.contains("gmail") || p.contains("mail") || p.contains("outlook")) return "email";
        if (p.contains("facebook") || p.contains("instagram") || p.contains("tiktok")) return "social";
        if (p.contains("youtube") || p.contains("netflix") || p.contains("spotify")) return "media";
        return "other";
    }

    private double estimateNotificationCO2(String category) {
        switch (category) {
            case "email": return 0.004; // 4g
            case "social": return 0.0002; // 0.2g
            default: return 0.0005;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // shutdown executor gracefully
        dbExecutor.shutdownNow();
        Log.d(TAG, "NotificationListener destroyed - executor shutdown");
    }
}