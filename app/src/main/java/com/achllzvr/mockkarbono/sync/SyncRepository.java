package com.achllzvr.mockkarbono.sync;

import android.content.Context;

import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.DailySummary;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.utils.PrefsManager;

import java.util.List;

public class SyncRepository {
    private final AppDatabase db;
    private final PrefsManager prefsManager;

    public SyncRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.prefsManager = new PrefsManager(context);
    }

    // --- Data Accessors for Sync Status ---

    public List<AppUsage> getUnsyncedAppUsage() {
        return db.appUsageDao().getUnsynced();
    }

    public List<NotificationEvent> getUnsyncedNotificationEvents() {
        return db.notificationEventDao().getUnsynced();
    }

    public List<DailySummary> getUnsyncedSummaries() {
        return db.dailySummaryDao().getUnsynced();
    }

    // Helper to check if user is logged in (has token)
    public boolean isAuthenticated() {
        return prefsManager.getToken() != null;
    }

    // Note: The actual conversion to JSON and uploading is now handled
    // by the SyncWorker + Retrofit (KarbonoApiService).
    // We removed the old Firestore 'toMap' methods as they are no longer needed.
}