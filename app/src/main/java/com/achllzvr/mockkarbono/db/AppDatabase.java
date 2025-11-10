package com.achllzvr.mockkarbono.db;

import android.content.Context;
import android.util.Log;

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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

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
                    Log.d("(DEBUG) AppDatabase", "Creating new database instance");
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .setQueryCallback((sqlQuery, bindArgs) -> {
                                Log.d("(DEBUG) AppDatabase", "Room SQL: " + sqlQuery + " args=" + bindArgs.toString());
                            }, Executors.newSingleThreadExecutor())
                            .build();
                }
            }
        }
        return instance;
    }
}