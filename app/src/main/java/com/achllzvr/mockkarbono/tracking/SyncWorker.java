package com.achllzvr.mockkarbono.tracking;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return Result.retry();
        if (uid == null){
            Log.d("(DEBUG) SyncWorker", "No user logged in");
        }

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        try {
            List<AppUsage> unsyncedUsage = db.appUsageDao().getUnsynced();
            List<NotificationEvent> unsyncedNotifs = db.notificationEventDao().getUnsynced();
            Log.d("(DEBUG) SyncWorker", "Unsynced usage: " + unsyncedUsage.size() + ", unsynced notifs: " + unsyncedNotifs.size());

            if (unsyncedUsage.isEmpty() && unsyncedNotifs.isEmpty()) {
                return Result.success();
            }

            CountDownLatch latch = new CountDownLatch(1);
            // We'll perform writes sequentially for simplicity.
            for (AppUsage u : unsyncedUsage) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("packageName", u.packageName);
                doc.put("category", u.category);
                doc.put("startTimeMs", u.startTimeMs);
                doc.put("endTimeMs", u.endTimeMs);
                doc.put("durationMs", u.durationMs);
                doc.put("estimatedKgCO2", u.estimatedKgCO2);
                doc.put("syncedAt", FieldValue.serverTimestamp());

                fs.collection("users").document(uid)
                        .collection("app_usage")
                        .add(doc)
                        .addOnSuccessListener(documentReference -> {
                            // mark local row as synced
                            u.synced = true;
                            db.appUsageDao().update(u);
                            Log.d("(DEBUG) SyncWorker", "Synced usage: " + u.packageName);
                        })
                        .addOnCompleteListener(task -> {
                            // if this was the last element, we'll latch later; but for simplicity we don't wait per doc
                        });
            }

            for (NotificationEvent n : unsyncedNotifs) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("packageName", n.packageName);
                doc.put("category", n.category);
                doc.put("timestampMs", n.timestampMs);
                doc.put("estimatedKgCO2", n.estimatedKgCO2);
                doc.put("syncedAt", FieldValue.serverTimestamp());

                fs.collection("users").document(uid)
                        .collection("notifications")
                        .add(doc)
                        .addOnSuccessListener(docRef -> {
                            n.synced = true;
                            db.notificationEventDao().update(n);
                            Log.d("(DEBUG) SyncWorker", "Synced notification: " + n.packageName);
                        });
            }

            // Rough wait to allow writes to finish (WorkManager time limit is generous for periodic tasks)
            // In production, better to use batch commits & callbacks. For demo, a small sleep helps sync.
            Thread.sleep(2000);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}