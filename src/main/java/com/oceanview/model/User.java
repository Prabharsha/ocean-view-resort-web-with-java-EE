package com.oceanview.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private String id;
    private String fname;
    private String lname;
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }

    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username + "', role='" + role + "'}";
    }
}

