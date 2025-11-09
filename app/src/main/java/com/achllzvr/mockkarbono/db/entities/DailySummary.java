package com.achllzvr.mockkarbono.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_summary")
public class DailySummary {
    @PrimaryKey
    public String date; // e.g., "2025-11-09"
    public double phoneKgCO2;
    public double applianceKgCO2;
    public double totalKgCO2;
    public boolean synced;
}