package com.achllzvr.mockkarbono.utils;

public class CarbonUtils {
    // Philippines Grid Intensity: ~0.8 kg CO2 per kWh (Average of 0.7-0.9)
    public static final double PH_GRID_KG_PER_KWH = 0.8;

    // --- Emission Factors (Device Energy + Network + Data Center) ---
    // Source: Official Karbono Documents

    // Social Media: ~0.2g CO2e per minute
    public static final double FACTOR_SOCIAL_KG_PER_MIN = 0.0002;

    // Mobile Gaming: ~0.02 kg CO2e per hour
    public static final double FACTOR_GAME_KG_PER_HOUR = 0.02;

    // Video/Streaming: High bandwidth. We treat it similarly to Social Media/Gaming.
    // If no specific stat exists, we can default to Social Media rate (0.2g/min)
    // or standard 5W usage (4g/hr) if conservative.
    // For MVP, let's align Video with Social due to high data center usage.
    public static final double FACTOR_VIDEO_KG_PER_MIN = 0.0002;

    // Standard Device Power (5 Watts) - used for "Other" apps (Electricity only)
    public static final double AVG_DEVICE_WATTS = 5.0;

    /**
     * Calculates the "True Carbon Cost" (Device + Cloud) based on category.
     */
    public static double calculateAppEmissions(String category, long durationMs) {
        if (category == null) category = "other";

        double minutes = durationMs / 60000.0;
        double hours = minutes / 60.0;

        switch (category) {
            case "social":
                return minutes * FACTOR_SOCIAL_KG_PER_MIN;
            case "video":
                return minutes * FACTOR_VIDEO_KG_PER_MIN;
            case "game":
                return hours * FACTOR_GAME_KG_PER_HOUR;
            default:
                // Fallback: Device electricity consumption only
                return calculateElectricityEmissions(AVG_DEVICE_WATTS, durationMs);
        }
    }

    // Base electricity calculation (used for fallback or pure energy display)
    public static double calculateElectricityEmissions(double watts, long durationMs) {
        double hours = durationMs / 3600000.0;
        double wh = watts * hours;
        return (wh / 1000.0) * PH_GRID_KG_PER_KWH;
    }

    // Helper for DB consistency (estimatedWh column)
    public static double wattsAndDurationToWh(double watts, long durationMs) {
        double hours = durationMs / 3600000.0;
        return watts * hours;
    }

    public static double whToKgCO2(double wh) {
        double kwh = wh / 1000.0;
        return kwh * PH_GRID_KG_PER_KWH;
    }
}