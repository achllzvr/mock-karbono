package com.achllzvr.mockkarbono.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_usage")
public class AppUsage {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String uuid; // client-generated unique id (UUID string)
    public String packageName;
    public String category; // e.g., social, video, screen
    public long startTimeMs;
    public long endTimeMs;
    public long durationMs;
    public double estimatedWh;
    public double estimatedKgCO2;
    public long clientCreatedAtMs; // when the event was created on device
    public boolean synced;
}