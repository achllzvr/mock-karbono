package com.achllzvr.mockkarbono.api.data.models;

import com.google.gson.annotations.SerializedName;

public class MarketplaceGoal {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title; // e.g. "Reforest Rizal"

    @SerializedName("description")
    public String description;

    @SerializedName("target_amount")
    public double targetAmount;

    @SerializedName("current_amount")
    public double currentAmount;

    @SerializedName("proof_image_url")
    public String proofImageUrl; // Null if not yet planted

    // Helper to calculate progress %
    public int getProgressPercentage() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount / targetAmount) * 100);
    }
}