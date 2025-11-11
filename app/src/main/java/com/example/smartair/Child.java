package com.example.smartair;

public class Child {
    private String name;
    private String dob;
    private String notes;

    public Child() {
    }

    public Child(String name, String dob, String notes) {
        this.name = name;
        this.dob = dob;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getNotes() {
        return notes;
    }
}
