package com.example.smartair.models;

public class User {
    private String uid;
    private String email;
    private UserRole role;
    private String name;
    private long createdAt;

    public User() {
    }

    public User(String uid, String email, UserRole role, String name) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}