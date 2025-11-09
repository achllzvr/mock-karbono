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

    @Query("SELECT * FROM daily_summary WHERE synced = 0")
    List<DailySummary> getUnsynced();

    // `DailySummary.date` is a string like "2025-11-09". Delete synced summaries older than the
    // provided cutoffDate (lexicographic comparison on ISO-8601 date strings works correctly).
    @Query("DELETE FROM daily_summary WHERE synced = 1 AND date < :cutoffDate")
    void deleteSyncedOlderThan(String cutoffDate);
}