// app/src/main/java/com/achllzvr/mockkarbono/db/dao/ApplianceDao.java
package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.achllzvr.mockkarbono.db.entities.ApplianceLog;

import java.util.List;

@Dao
public interface ApplianceDao {
    @Insert
    long insert(ApplianceLog log);

    @Update
    void update(ApplianceLog log);

    @Query("SELECT * FROM appliance_log ORDER BY clientCreatedAtMs DESC")
    List<ApplianceLog> getAll();

    @Query("SELECT * FROM appliance_log WHERE synced = 0 ORDER BY clientCreatedAtMs ASC")
    List<ApplianceLog> getUnsynced();

    @Query("UPDATE appliance_log SET synced = 1 WHERE uuid IN(:uuids)")
    void markSyncedByUuid(List<String> uuids);

    @Query("SELECT COUNT(*) FROM appliance_log WHERE synced = 0")
    int countUnsynced();

    @Query("DELETE FROM appliance_log WHERE uuid = :uuid")
    void deleteByUuid(String uuid);
}