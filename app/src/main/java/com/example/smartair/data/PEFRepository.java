package com.example.smartair.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.smartair.model.PEFEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing PEF (Peak Expiratory Flow) entries.
 */
public class PEFRepository {
    private static final String TAG = "PEFRepository";
    private static final String PREFS_NAME = "SmartAir_PEF";
    private static final String KEY_PEF_ENTRIES = "pef_entries";
    
    private final SharedPreferences sharedPreferences;
    
    public PEFRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    public void addPEFEntry(PEFEntry entry) {
        if (entry.getId() == null || entry.getId().isEmpty()) {
            entry.setId(UUID.randomUUID().toString());
        }
        
        List<PEFEntry> entries = getAllPEFEntries();
        entries.add(entry);
        savePEFEntries(entries);
    }

    public List<PEFEntry> getAllPEFEntries() {
        String jsonString = sharedPreferences.getString(KEY_PEF_ENTRIES, null);
        if (jsonString == null || jsonString.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PEFEntry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PEFEntry entry = PEFEntry.fromJSON(jsonObject);
                entries.add(entry);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading PEF entries from JSON", e);
        }
        
        // Sort by timestamp, newest first
        Collections.sort(entries, (e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        
        return entries;
    }

    public List<PEFEntry> getPEFEntriesInRange(long startTime, long endTime) {
        List<PEFEntry> allEntries = getAllPEFEntries();
        List<PEFEntry> filtered = new ArrayList<>();
        
        for (PEFEntry entry : allEntries) {
            if (entry.getTimestamp() >= startTime && entry.getTimestamp() <= endTime) {
                filtered.add(entry);
            }
        }
        
        return filtered;
    }

    public PEFEntry getMostRecentPEFEntry() {
        List<PEFEntry> entries = getAllPEFEntries();
        return entries.isEmpty() ? null : entries.get(0);
    }

    public void deletePEFEntry(String entryId) {
        List<PEFEntry> entries = getAllPEFEntries();
        entries.removeIf(entry -> entry.getId().equals(entryId));
        savePEFEntries(entries);
    }

    private void savePEFEntries(List<PEFEntry> entries) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (PEFEntry entry : entries) {
                jsonArray.put(entry.toJSON());
            }
            sharedPreferences.edit().putString(KEY_PEF_ENTRIES, jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving PEF entries to JSON", e);
        }
    }
}

