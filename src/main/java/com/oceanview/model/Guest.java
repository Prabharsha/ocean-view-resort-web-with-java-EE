package com.oceanview.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Guest implements Serializable {
    private String id;
    private String name;
    private String address;
    private String contact;
    private String email;
    private String nic;
    private int loyaltyPts;
    private LocalDateTime createdAt;

    public Guest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public int getLoyaltyPts() { return loyaltyPts; }
    public void setLoyaltyPts(int loyaltyPts) { this.loyaltyPts = loyaltyPts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Guest{id='" + id + "', name='" + name + "', email='" + email + "'}";
    }
}

