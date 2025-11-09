package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.achllzvr.mockkarbono.db.entities.AppUsage;

import java.util.List;

@Dao
public interface AppUsageDao {
    @Insert
    long insert(AppUsage usage);

    @Update
    void update(AppUsage usage);

    @Query("SELECT * FROM app_usage WHERE synced = 0")
    List<AppUsage> getUnsynced();

    @Query("DELETE FROM app_usage WHERE synced = 1 AND endTimeMs < :cutoffMs")
    void deleteSyncedOlderThan(long cutoffMs);
}