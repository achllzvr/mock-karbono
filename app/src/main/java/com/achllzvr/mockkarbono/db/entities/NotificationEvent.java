package com.achllzvr.mockkarbono.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notification_event")
public class NotificationEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String packageName;
    public String category;
    public long timestampMs;
    public double estimatedKgCO2;
    public boolean synced;
}