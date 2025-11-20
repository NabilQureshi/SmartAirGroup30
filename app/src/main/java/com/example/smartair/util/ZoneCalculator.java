package com.example.smartair.util;

/**
 * Utility class for calculating asthma zones based on Personal Best (PB) and current PEF.
 * Zone thresholds:
 * - Green: ≥80% of PB
 * - Yellow: 50-79% of PB
 * - Red: <50% of PB
 */
public class ZoneCalculator {
    
    public enum Zone {
        GREEN,
        YELLOW,
        RED,
        UNKNOWN // When PB is not set
    }


    public static Zone calculateZone(Integer currentPef, Integer personalBest) {
        if (personalBest == null || personalBest <= 0 || currentPef == null || currentPef < 0) {
            return Zone.UNKNOWN;
        }

        double percentage = (currentPef * 100.0) / personalBest;

        if (percentage >= 80) {
            return Zone.GREEN;
        } else if (percentage >= 50) {
            return Zone.YELLOW;
        } else {
            return Zone.RED;
        }
    }

    public static int[] getZoneThresholds(Integer personalBest) {
        if (personalBest == null || personalBest <= 0) {
            return new int[]{0, 0, 0};
        }

        int greenThreshold = (int) Math.round(personalBest * 0.80);
        int yellowThreshold = (int) Math.round(personalBest * 0.50);
        int redThreshold = 0; // Red is anything below yellow

        return new int[]{greenThreshold, yellowThreshold, redThreshold};
    }

    public static String getZoneName(Zone zone) {
        switch (zone) {
            case GREEN:
                return "Green";
            case YELLOW:
                return "Yellow";
            case RED:
                return "Red";
            case UNKNOWN:
            default:
                return "Unknown";
        }
    }
}

