package com.achllzvr.mockkarbono.utils;

import com.achllzvr.mockkarbono.db.entities.CarbonReference;
import java.util.List;

public class CarbonUtils {

    // Fallback if app is unknown (General Browsing)
    private static final double DEFAULT_FACTOR = 0.0015;

    /**
     * Calculates CO2 based on dynamic factors.
     */
    public static double calculateCO2(String packageName, long durationSeconds, List<CarbonReference> references) {
        if (durationSeconds <= 0) return 0;

        double factor = DEFAULT_FACTOR;

        if (references != null) {
            for (CarbonReference ref : references) {
                if (ref.packageName.equals(packageName)) {
                    factor = ref.co2FactorPerMin;
                    break;
                }
            }
        }

        double minutes = durationSeconds / 60.0;
        return minutes * factor;
    }
}