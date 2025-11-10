package com.achllzvr.mockkarbono.db.dao;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT * FROM app_usage WHERE synced = 0 ORDER BY clientCreatedAtMs ASC")
    List<AppUsage> getUnsynced();

    @Query("SELECT * FROM app_usage ORDER BY clientCreatedAtMs DESC LIMIT :limit")
    List<AppUsage> getLatest(int limit);

    @Query("SELECT * FROM app_usage ORDER BY clientCreatedAtMs DESC")
    LiveData<List<AppUsage>> observeAll();

    @Query("UPDATE app_usage SET synced = 1 WHERE uuid IN(:uuids)")
    void markSyncedByUuid(List<String> uuids);

    @Query("SELECT COUNT(*) FROM app_usage WHERE synced = 0")
    int countUnsynced();
}