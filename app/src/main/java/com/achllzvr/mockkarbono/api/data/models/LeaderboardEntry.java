package com.achllzvr.mockkarbono.api.data.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {
    @SerializedName("id")
    public int userId;

    @SerializedName("username")
    public String username;

    @SerializedName("total_carbon_saved_kg")
    public double carbonSaved;

    @SerializedName("current_streak")
    public int streak;

    public int rank;
}