package com.example.smartair.util;

/**
 * Helper for computing asthma zones based on a child's Personal Best (PB)
 * and the current Peak Expiratory Flow (PEF) reading.
 */
public final class PEFZoneCalculator {

    private PEFZoneCalculator() {
        // Utility class
    }

    public enum Zone {
        GREEN,
        YELLOW,
        RED,
        UNKNOWN
    }

    public static class ZoneResult {
        private final Zone zone;
        private final float percentOfPersonalBest;
        private final int pefValue;
        private final boolean ready;

        private ZoneResult(Zone zone, float percentOfPersonalBest, int pefValue, boolean ready) {
            this.zone = zone;
            this.percentOfPersonalBest = percentOfPersonalBest;
            this.pefValue = pefValue;
            this.ready = ready;
        }

        public Zone getZone() {
            return zone;
        }

        public float getPercentOfPersonalBest() {
            return percentOfPersonalBest;
        }

        public int getPefValue() {
            return pefValue;
        }

        public boolean isReady() {
            return ready;
        }
    }

    public static ZoneResult calculateZone(Integer pefValue, Integer personalBest) {
        if (pefValue == null || pefValue <= 0 || personalBest == null || personalBest <= 0) {
            return new ZoneResult(Zone.UNKNOWN, 0f, 0, false);
        }

        float percent = (pefValue * 100f) / personalBest;
        Zone zone;
        if (percent >= 80f) {
            zone = Zone.GREEN;
        } else if (percent >= 50f) {
            zone = Zone.YELLOW;
        } else {
            zone = Zone.RED;
        }
        return new ZoneResult(zone, percent, pefValue, true);
    }
}
