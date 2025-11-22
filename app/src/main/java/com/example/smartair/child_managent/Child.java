package com.example.smartair.child_managent;

public class Child {

    private String id;
    private String username;
    private String password;
    private String name;
    private String dob;
    private String notes;

    public Child() {}

    public Child(String id, String username, String password, String name, String dob, String notes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public String getNotes() { return notes; }
}
