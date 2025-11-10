// app/src/main/java/com/achllzvr/mockkarbono/db/dao/DailySummaryDao.java
package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.achllzvr.mockkarbono.db.entities.DailySummary;

import java.util.List;

@Dao
public interface DailySummaryDao {
    @Insert
    long insert(DailySummary summary);

    @Update
    void update(DailySummary summary);

    @Query("SELECT * FROM daily_summary ORDER BY date DESC")
    List<DailySummary> getAll();

    @Query("SELECT * FROM daily_summary WHERE synced = 0 ORDER BY clientCreatedAtMs ASC")
    List<DailySummary> getUnsynced();

    @Query("UPDATE daily_summary SET synced = 1 WHERE date IN(:dates)")
    void markSyncedByDate(List<String> dates);

    @Query("SELECT COUNT(*) FROM daily_summary WHERE synced = 0")
    int countUnsynced();
}