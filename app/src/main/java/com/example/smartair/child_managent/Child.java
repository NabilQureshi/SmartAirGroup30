package com.example.smartair.child_managent;

public class Child {

    private String id;
    private String uid;       // FirebaseAuth 的 uid
    private String username;
    private String password;
    private String email;
    private String name;
    private String dob;
    private String notes;
    private String role;

    // 无参构造函数，必须有
    public Child() {}

    // 带参构造函数
    public Child(String id, String uid, String username, String password, String email,
                 String name, String dob, String notes, String role) {
        this.id = id;
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.dob = dob;
        this.notes = notes;
        this.role = role;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
