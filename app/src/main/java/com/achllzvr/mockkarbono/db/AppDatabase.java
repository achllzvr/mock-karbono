package com.achllzvr.mockkarbono.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.achllzvr.mockkarbono.db.dao.AppUsageDao;
import com.achllzvr.mockkarbono.db.dao.CarbonReferenceDao;
import com.achllzvr.mockkarbono.db.dao.DailySummaryDao;
import com.achllzvr.mockkarbono.db.dao.NotificationEventDao;
import com.achllzvr.mockkarbono.db.entities.AppUsage;
import com.achllzvr.mockkarbono.db.entities.CarbonReference;
import com.achllzvr.mockkarbono.db.entities.DailySummary;
import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

@Database(entities = {
        AppUsage.class,
        DailySummary.class,
        NotificationEvent.class,
        CarbonReference.class
},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract AppUsageDao appUsageDao();
    public abstract DailySummaryDao dailySummaryDao();
    public abstract NotificationEventDao notificationEventDao();
    public abstract CarbonReferenceDao carbonReferenceDao(); // NEW

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "karbono_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}