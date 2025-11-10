package com.achllzvr.mockkarbono.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appliance_log")
public class ApplianceLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String uuid;
    public String name;
    public int typicalWattage;
    public double hoursPerDay;
    public double estimatedKgCO2PerDay;
    public long clientCreatedAtMs;
    public boolean synced;

}