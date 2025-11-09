package com.achllzvr.mockkarbono.utils;

public class CarbonUtils {
    public static double GRID_KG_PER_KWH = 0.8; // fallback: replace with researched PH grid intensity

    // Convert watts + duration (ms) to Wh
    public static double wattsAndDurationToWh(double watts, long durationMs) {
        double hours = durationMs / 1000.0 / 3600.0;
        return watts * hours;
    }

    public static double whToKgCO2(double wh) {
        double kwh = wh / 1000.0;
        return kwh * GRID_KG_PER_KWH;
    }
}