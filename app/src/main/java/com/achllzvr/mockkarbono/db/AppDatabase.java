package com.achllzvr.mockkarbono.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.achllzvr.mockkarbono.db.dao.AppUsageDao;
import com.achllzvr.mockkarbono.db.dao.NotificationEventDao;
import com.achllzvr.mockkarbono.db.dao.ApplianceDao;
import com.achllzvr.mockkarbono.db.dao.DailySummaryDao;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;
import com.achllzvr.mockkarbono.db.entities.ApplianceLog;
import com.achllzvr.mockkarbono.db.entities.DailySummary;

@Database(entities = {AppUsage.class, NotificationEvent.class, ApplianceLog.class, DailySummary.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "mockkarbono.db";
    private static volatile AppDatabase instance;

    public abstract AppUsageDao appUsageDao();
    public abstract NotificationEventDao notificationEventDao();
    public abstract ApplianceDao applianceDao();
    public abstract DailySummaryDao dailySummaryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}