// app/src/main/java/com/achllzvr/mockkarbono/db/dao/NotificationEventDao.java
package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.List;

@Dao
public interface NotificationEventDao {
    @Insert
    long insert(NotificationEvent event);

    @Update
    void update(NotificationEvent event);

    @Query("SELECT * FROM notification_event WHERE synced = 0")
    List<NotificationEvent> getUnsynced();

    // Deletes synced notification events older than the provided cutoff (uses timestampMs field).
    @Query("DELETE FROM notification_event WHERE synced = 1 AND timestampMs < :cutoffMs")
    void deleteSyncedOlderThan(long cutoffMs);
}