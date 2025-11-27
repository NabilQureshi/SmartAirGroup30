package com.example.smartair.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.smartair.model.Child;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing child data.
 * Uses SharedPreferences with JSONArray.
 */
public class ChildRepository {
    private static final String TAG = "ChildRepository";
    private static final String PREFS_NAME = "SmartAir_Children";
    private static final String KEY_CHILDREN = "children_list";
    
    private final SharedPreferences sharedPreferences;
    
    public ChildRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addChild(Child child) {
        if (child.getId() == null || child.getId().isEmpty()) {
            child.setId(UUID.randomUUID().toString());
        }
        
        List<Child> children = getAllChildren();
        children.add(child);
        saveChildren(children);
    }


    public void updateChild(Child child) {
        List<Child> children = getAllChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getId().equals(child.getId())) {
                children.set(i, child);
                saveChildren(children);
                return;
            }
        }
    }

    public void updatePersonalBest(String childId, Integer personalBest) {
        Child child = getChildById(childId);
        if (child != null) {
            child.setPersonalBest(personalBest);
            updateChild(child);
        }
    }

    public Child getChildById(String childId) {
        List<Child> children = getAllChildren();
        for (Child child : children) {
            if (child.getId().equals(childId)) {
                return child;
            }
        }
        return null;
    }

    public List<Child> getAllChildren() {
        String jsonString = sharedPreferences.getString(KEY_CHILDREN, null);
        if (jsonString == null || jsonString.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Child> children = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Child child = Child.fromJSON(jsonObject);
                children.add(child);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading children from JSON", e);
        }
        
        return children;
    }

    public void deleteChild(String childId) {
        List<Child> children = getAllChildren();
        children.removeIf(child -> child.getId().equals(childId));
        saveChildren(children);
    }

    private void saveChildren(List<Child> children) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Child child : children) {
                jsonArray.put(child.toJSON());
            }
            sharedPreferences.edit().putString(KEY_CHILDREN, jsonArray.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving children to JSON", e);
        }
    }
}

