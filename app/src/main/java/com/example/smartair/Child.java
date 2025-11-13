package com.example.smartair;

public class Child {

    private String id;     // Firestore document ID
    private String name;
    private String dob;
    private String notes;

    public Child() {
        // Firestore requires empty constructor
    }

    public Child(String id, String name, String dob, String notes) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
    }

    // ID (Firestore document ID)
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Name
    public String getName() {
        return name;
    }

    // DOB
    public String getDob() {
        return dob;
    }

    // Notes
    public String getNotes() {
        return notes;
    }
}
