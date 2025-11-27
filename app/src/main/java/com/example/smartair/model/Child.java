package com.example.smartair.model;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Represents a child user in the SmartAir app.
 * Contains personal information and health metrics including Personal Best (PB).
 */
public class Child implements Serializable {
    private static final String TAG = "Child";
    
    private String id;
    private String name;
    private String dateOfBirth; // Format: YYYY-MM-DD
    private Integer personalBest; // Personal Best PEF value (null if not set)
    private String notes; // Optional notes

    public Child() {
    }

    public Child(String id, String name, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.personalBest = null;
        this.notes = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getPersonalBest() {
        return personalBest;
    }

    public void setPersonalBest(Integer personalBest) {
        this.personalBest = personalBest;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean hasPersonalBest() {
        return personalBest != null && personalBest > 0;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id != null ? id : "");
            json.put("name", name != null ? name : "");
            json.put("dateOfBirth", dateOfBirth != null ? dateOfBirth : "");
            json.put("personalBest", personalBest != null ? personalBest : JSONObject.NULL);
            json.put("notes", notes != null ? notes : "");
        } catch (JSONException e) {
            Log.e(TAG, "Error converting Child to JSON", e);
        }
        return json;
    }

    public static Child fromJSON(JSONObject json) {
        Child child = new Child();
        try {
            child.setId(json.optString("id", ""));
            child.setName(json.optString("name", ""));
            child.setDateOfBirth(json.optString("dateOfBirth", ""));
            if (!json.isNull("personalBest")) {
                child.setPersonalBest(json.optInt("personalBest", 0));
            }
            child.setNotes(json.optString("notes", ""));
        } catch (Exception e) {
            Log.e(TAG, "Error creating Child from JSON", e);
        }
        return child;
    }
}

