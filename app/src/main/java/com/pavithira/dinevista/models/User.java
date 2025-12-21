package com.pavithira.dinevista.models;

public class User {
    private String uid; // add this
    private String email;
    private String password;
    private String name;
    private String address;
    private String username;
    private String phone;
    private String role;

    // Constructor including UID
    public User(String uid, String email, String password, String name, String address, String username, String phone, String role) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        this.username = username;
        this.phone = phone;
        this.role = role;
    }

    // Getter and setter for UID
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    // Other getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getUsername() { return username; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}
