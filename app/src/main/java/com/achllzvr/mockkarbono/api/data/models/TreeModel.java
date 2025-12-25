package com.achllzvr.mockkarbono.api.data.models;

import com.google.gson.annotations.SerializedName;

public class TreeModel {
    @SerializedName("tree_model_id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("cover_image_url")
    public String imageUrl;

    @SerializedName("description")
    public String description;

    // API returns CO2 in Kilograms (KG), e.g. 150.0
    @SerializedName("co2_kg")
    public double co2Kg;

    @SerializedName("price")
    public Price price;

    @SerializedName("location")
    public Location location;

    // Helper to get tonnes if you still need it for UI
    public double getCo2Tonnes() {
        return co2Kg / 1000.0;
    }

    // Nested Price Object
    public static class Price {
        @SerializedName("amount_usd_onetime_cents")
        public int usdCents;
    }

    // Nested Location Object
    public static class Location {
        @SerializedName("latitude")
        public double latitude;

        @SerializedName("longitude")
        public double longitude;

        @SerializedName("country")
        public String country;
    }
}