package com.achllzvr.mockkarbono.sync;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.achllzvr.mockkarbono.db.AppDatabase;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.db.entities.DailySummary;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SyncRepository {
    private final Context context;
    private final FirebaseFirestore firestore;
    private final String uid;
    private final AppDatabase db;

    public SyncRepository(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.uid = FirebaseAuth.getInstance().getUid();
        this.db = AppDatabase.getInstance(context);
    }

    public List<AppUsage> getUnsyncedAppUsage() {
        return db.appUsageDao().getUnsynced();
    }

    public List<NotificationEvent> getUnsyncedNotificationEvents() {
        return db.notificationEventDao().getUnsynced();
    }

    public List<ApplianceLog> getUnsyncedAppliances() {
        return db.applianceDao().getUnsynced();
    }

    public List<DailySummary> getUnsyncedSummaries() {
        return db.dailySummaryDao().getUnsynced();
    }

    public static Map<String, Object> toMap(AppUsage u) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", u.uuid);
        m.put("packageName", u.packageName);
        m.put("category", u.category);
        m.put("startTimeMs", u.startTimeMs);
        m.put("endTimeMs", u.endTimeMs);
        m.put("durationMs", u.durationMs);
        m.put("estimatedKgCO2", u.estimatedKgCO2);
        m.put("clientCreatedAtMs", u.clientCreatedAtMs);
        m.put("syncedAtMs", System.currentTimeMillis());
        return m;
    }

    public static Map<String, Object> notifToMap(NotificationEvent n) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", n.uuid);
        m.put("packageName", n.packageName);
        m.put("category", n.category);
        m.put("timestampMs", n.timestampMs);
        m.put("estimatedKgCO2", n.estimatedKgCO2);
        m.put("clientCreatedAtMs", n.clientCreatedAtMs);
        m.put("syncedAtMs", System.currentTimeMillis());
        return m;
    }

    public static Map<String, Object> applianceToMap(ApplianceLog a) {
        Map<String, Object> m = new HashMap<>();
        m.put("uuid", a.uuid);
        m.put("name", a.name);
        m.put("typicalWattage", a.typicalWattage);
        m.put("hoursPerDay", a.hoursPerDay);
        m.put("estimatedKgCO2PerDay", a.estimatedKgCO2PerDay);
        m.put("clientCreatedAtMs", a.clientCreatedAtMs);
        m.put("syncedAtMs", System.currentTimeMillis());
        return m;
    }

    public static Map<String, Object> summaryToMap(DailySummary s) {
        Map<String, Object> m = new HashMap<>();
        m.put("date", s.date);
        m.put("phoneKgCO2", s.phoneKgCO2);
        m.put("applianceKgCO2", s.applianceKgCO2);
        m.put("totalKgCO2", s.totalKgCO2);
        m.put("clientCreatedAtMs", s.clientCreatedAtMs);
        m.put("syncedAtMs", System.currentTimeMillis());
        return m;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public String getUid() {
        return uid;
    }
}