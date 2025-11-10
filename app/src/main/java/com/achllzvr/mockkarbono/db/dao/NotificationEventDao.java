// app/src/main/java/com/achllzvr/mockkarbono/db/dao/NotificationEventDao.java
package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.achllzvr.mockkarbono.db.entities.NotificationEvent;

import java.util.List;

@Dao
public interface NotificationEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(NotificationEvent event);

    @Update
    void update(NotificationEvent event);

    @Query("SELECT * FROM notification_event WHERE synced = 0 ORDER BY clientCreatedAtMs ASC")
    List<NotificationEvent> getUnsynced();

    @Query("UPDATE notification_event SET synced = 1 WHERE uuid IN(:uuids)")
    void markSyncedByUuid(List<String> uuids);

    @Query("SELECT COUNT(*) FROM notification_event WHERE synced = 0")
    int countUnsynced();
}