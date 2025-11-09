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

    @Query("SELECT * FROM appliance_log WHERE synced = 0")
    List<ApplianceLog> getUnsynced();

    @Query("DELETE FROM appliance_log WHERE synced = 1")
    void deleteSynced();
}