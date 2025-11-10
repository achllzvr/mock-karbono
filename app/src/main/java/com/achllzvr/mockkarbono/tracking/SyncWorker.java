package com.achllzvr.mockkarbono.tracking;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.dao.AppUsageDao;
import com.achllzvr.mockkarbono.db.dao.NotificationEventDao;
import com.achllzvr.mockkarbono.db.dao.ApplianceDao;
import com.achllzvr.mockkarbono.db.dao.DailySummaryDao;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.db.entities.DailySummary;
import com.achllzvr.mockkarbono.sync.SyncRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncWorker extends Worker {
    private static final String TAG = "KarbonoSyncWorker";
    private static final int FIRESTORE_BATCH_LIMIT = 400; // safe margin under 500 limit

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("(DEBUG) SyncWorker", "DB path: " + getApplicationContext().getDatabasePath("karbono.db").getAbsolutePath());

        final String uid = FirebaseAuth.getInstance().getUid();
        Log.d("(DEBUG) SyncWorker", "Firebase uid=" + FirebaseAuth.getInstance().getUid());
        if (uid == null) {
            Log.w(TAG, "(DEBUG) SyncWorker: no firebase uid; retrying later");
            return Result.retry();
        }

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        AppUsageDao usageDao = db.appUsageDao();
        NotificationEventDao notifDao = db.notificationEventDao();
        ApplianceDao applianceDao = db.applianceDao();
        DailySummaryDao summaryDao = db.dailySummaryDao();

        List<AppUsage> unsyncedUsage = usageDao.getUnsynced();
        List<NotificationEvent> unsyncedNotifs = notifDao.getUnsynced();
        List<ApplianceLog> unsyncedAppliances = applianceDao.getUnsynced();
        List<DailySummary> unsyncedSummaries = summaryDao.getUnsynced();

        Log.d("(DEBUG) SyncWorker", "Unsynced counts: usage=" + unsyncedUsage.size() +
                " notifs=" + unsyncedNotifs.size() +
                " appliances=" + unsyncedAppliances.size() +
                " summaries=" + unsyncedSummaries.size());

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        try {
            List<String> syncedUsageUuids = new ArrayList<>();
            List<String> syncedNotifUuids = new ArrayList<>();
            List<String> syncedApplianceUuids = new ArrayList<>();
            List<String> syncedSummaryDates = new ArrayList<>();

            // Helper to process list in batches for a collection
            // 1) AppUsage batches
            for (int i = 0; i < unsyncedUsage.size(); i += FIRESTORE_BATCH_LIMIT) {
                int end = Math.min(i + FIRESTORE_BATCH_LIMIT, unsyncedUsage.size());
                WriteBatch batch = fs.batch();
                List<AppUsage> sub = unsyncedUsage.subList(i, end);
                for (AppUsage u : sub) {
                    DocumentReference docRef = fs.collection("users").document(uid)
                            .collection("app_usage").document(u.uuid);
                    Map<String, Object> map = SyncRepository.toMap(u);
                    batch.set(docRef, map);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] success = {false};
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("(DEBUG) SyncWorker", "Batch commit success for app usage items: " + sub.size());
                            for (AppUsage u : sub) syncedUsageUuids.add(u.uuid);
                            success[0] = true;
                            latch.countDown();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("(DEBUG) SyncWorker", "Batch commit failed for app usage", e);
                            latch.countDown();
                        });
                latch.await();
                if (!success[0]) {
                    Log.w("(DEBUG) SyncWorker", "A usage batch failed; aborting sync to retry later");
                    return Result.retry();
                }
            }

            // 2) NotificationEvent batches
            for (int i = 0; i < unsyncedNotifs.size(); i += FIRESTORE_BATCH_LIMIT) {
                int end = Math.min(i + FIRESTORE_BATCH_LIMIT, unsyncedNotifs.size());
                WriteBatch batch = fs.batch();
                List<NotificationEvent> sub = unsyncedNotifs.subList(i, end);
                for (NotificationEvent n : sub) {
                    DocumentReference docRef = fs.collection("users").document(uid)
                            .collection("notifications").document(n.uuid);
                    Map<String, Object> map = SyncRepository.notifToMap(n);
                    batch.set(docRef, map);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] success = {false};
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("(DEBUG) SyncWorker", "Batch commit success for notification items: " + sub.size());
                            for (NotificationEvent n : sub) syncedNotifUuids.add(n.uuid);
                            success[0] = true;
                            latch.countDown();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("(DEBUG) SyncWorker", "Batch commit failed for notifications", e);
                            latch.countDown();
                        });
                latch.await();
                if (!success[0]) {
                    Log.w("(DEBUG) SyncWorker", "A notifications batch failed; aborting sync to retry later");
                    return Result.retry();
                }
            }

            // 3) ApplianceLog batches
            for (int i = 0; i < unsyncedAppliances.size(); i += FIRESTORE_BATCH_LIMIT) {
                int end = Math.min(i + FIRESTORE_BATCH_LIMIT, unsyncedAppliances.size());
                WriteBatch batch = fs.batch();
                List<ApplianceLog> sub = unsyncedAppliances.subList(i, end);
                for (ApplianceLog a : sub) {
                    DocumentReference docRef = fs.collection("users").document(uid)
                            .collection("appliances").document(a.uuid);
                    Map<String, Object> map = SyncRepository.applianceToMap(a);
                    batch.set(docRef, map);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] success = {false};
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("(DEBUG) SyncWorker", "Batch commit success for appliance items: " + sub.size());
                            for (ApplianceLog a : sub) syncedApplianceUuids.add(a.uuid);
                            success[0] = true;
                            latch.countDown();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("(DEBUG) SyncWorker", "Batch commit failed for appliances", e);
                            latch.countDown();
                        });
                latch.await();
                if (!success[0]) {
                    Log.w("(DEBUG) SyncWorker", "An appliance batch failed; aborting sync to retry later");
                    return Result.retry();
                }
            }

            // 4) DailySummary batches (keyed by date)
            for (int i = 0; i < unsyncedSummaries.size(); i += FIRESTORE_BATCH_LIMIT) {
                int end = Math.min(i + FIRESTORE_BATCH_LIMIT, unsyncedSummaries.size());
                WriteBatch batch = fs.batch();
                List<DailySummary> sub = unsyncedSummaries.subList(i, end);
                for (DailySummary s : sub) {
                    DocumentReference docRef = fs.collection("users").document(uid)
                            .collection("daily_summary").document(s.date);
                    Map<String, Object> map = SyncRepository.summaryToMap(s);
                    batch.set(docRef, map);
                }
                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] success = {false};
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("(DEBUG) SyncWorker", "Batch commit success for summaries: " + sub.size());
                            for (DailySummary s : sub) syncedSummaryDates.add(s.date);
                            success[0] = true;
                            latch.countDown();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("(DEBUG) SyncWorker", "Batch commit failed for summaries", e);
                            latch.countDown();
                        });
                latch.await();
                if (!success[0]) {
                    Log.w("(DEBUG) SyncWorker", "A summaries batch failed; aborting sync to retry later");
                    return Result.retry();
                }
            }

            // Mark local rows as synced
            if (!syncedUsageUuids.isEmpty()) {
                usageDao.markSyncedByUuid(syncedUsageUuids);
                Log.d("(DEBUG) SyncWorker", "Marked local usage rows synced: " + syncedUsageUuids.size());
            }
            if (!syncedNotifUuids.isEmpty()) {
                notifDao.markSyncedByUuid(syncedNotifUuids);
                Log.d("(DEBUG) SyncWorker", "Marked local notification rows synced: " + syncedNotifUuids.size());
            }
            if (!syncedApplianceUuids.isEmpty()) {
                applianceDao.markSyncedByUuid(syncedApplianceUuids);
                Log.d("(DEBUG) SyncWorker", "Marked local appliance rows synced: " + syncedApplianceUuids.size());
            }
            if (!syncedSummaryDates.isEmpty()) {
                summaryDao.markSyncedByDate(syncedSummaryDates);
                Log.d("(DEBUG) SyncWorker", "Marked local summaries synced: " + syncedSummaryDates.size());
            }

            Log.d("(DEBUG) SyncWorker", "SyncWorker: all batches succeeded");
            return Result.success();
        } catch (InterruptedException ie) {
            Log.e("(DEBUG) SyncWorker", "Sync interrupted", ie);
            Thread.currentThread().interrupt();
            return Result.retry();
        } catch (Exception e) {
            Log.e("(DEBUG) SyncWorker", "Unhandled exception in SyncWorker", e);
            return Result.retry();
        }
    }
}