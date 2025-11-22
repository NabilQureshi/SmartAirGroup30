package com.example.smartair.model;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Represents a Peak Expiratory Flow (PEF) entry.
 * Can be tagged as pre-medicine or post-medicine to track how medicine affects breathing.
 */
public class PEFEntry implements Serializable {
    private static final String TAG = "PEFEntry";
    
    private String id;
    private int pefValue; // PEF value in L/min
    private long timestamp; // Unix timestamp in milliseconds
    private MedicineTag medicineTag; // Pre-medicine or post-medicine tag

    public enum MedicineTag {
        PRE_MEDICINE("Before Medicine"),
        POST_MEDICINE("After Medicine"),
        NONE("No Medicine Tag");

        private final String displayName;

        MedicineTag(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static MedicineTag fromString(String value) {
            try {
                return valueOf(value);
            } catch (Exception e) {
                return NONE;
            }
        }
    }

    public PEFEntry() {
        // no-arg
    }

    public PEFEntry(int pefValue, long timestamp, MedicineTag medicineTag) {
        this.pefValue = pefValue;
        this.timestamp = timestamp;
        this.medicineTag = medicineTag != null ? medicineTag : MedicineTag.NONE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPefValue() {
        return pefValue;
    }

    public void setPefValue(int pefValue) {
        this.pefValue = pefValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MedicineTag getMedicineTag() {
        return medicineTag;
    }

    public void setMedicineTag(MedicineTag medicineTag) {
        this.medicineTag = medicineTag != null ? medicineTag : MedicineTag.NONE;
    }

    public boolean isPreMedicine() {
        return medicineTag == MedicineTag.PRE_MEDICINE;
    }

    public boolean isPostMedicine() {
        return medicineTag == MedicineTag.POST_MEDICINE;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id != null ? id : "");
            json.put("pefValue", pefValue);
            json.put("timestamp", timestamp);
            json.put("medicineTag", medicineTag != null ? medicineTag.name() : MedicineTag.NONE.name());
        } catch (JSONException e) {
            Log.e(TAG, "Error converting PEFEntry to JSON", e);
        }
        return json;
    }

    public static PEFEntry fromJSON(JSONObject json) {
        PEFEntry entry = new PEFEntry();
        try {
            entry.setId(json.optString("id", ""));
            entry.setPefValue(json.optInt("pefValue", 0));
            entry.setTimestamp(json.optLong("timestamp", System.currentTimeMillis()));
            String tagStr = json.optString("medicineTag", MedicineTag.NONE.name());
            entry.setMedicineTag(MedicineTag.fromString(tagStr));
        } catch (Exception e) {
            Log.e(TAG, "Error creating PEFEntry from JSON", e);
        }
        return entry;
    }
}

