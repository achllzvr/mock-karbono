package com.achllzvr.mockkarbono.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "carbon_references")
public class CarbonReference {
    @PrimaryKey
    @NonNull
    @SerializedName("package_name")
    public String packageName;

    @SerializedName("category")
    public String category; // e.g., "HIGH_DATA"

    @SerializedName("co2_factor_per_min")
    public double co2FactorPerMin;

    @SerializedName("citation_source")
    public String citationSource;

    public CarbonReference(@NonNull String packageName, String category, double co2FactorPerMin) {
        this.packageName = packageName;
        this.category = category;
        this.co2FactorPerMin = co2FactorPerMin;
    }
}