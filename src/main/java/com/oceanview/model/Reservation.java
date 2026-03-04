package com.oceanview.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation implements Serializable {
    private String id;
    private String reservationNo;
    private String guestId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private String status;
    private double totalAmount;
    private String specialRequests;
    private String createdBy;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    // Transient display fields (not persisted, populated by controller for list views)
    private String guestName;
    private String roomNumber;

    public Reservation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservationNo() { return reservationNo; }
    public void setReservationNo(String reservationNo) { this.reservationNo = reservationNo; }

    public String getGuestId() { return guestId; }
    public void setGuestId(String guestId) { this.guestId = guestId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNumGuests() { return numGuests; }
    public void setNumGuests(int numGuests) { this.numGuests = numGuests; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Convenience: number of nights between check-in and check-out. */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return checkOutDate.toEpochDay() - checkInDate.toEpochDay();
    }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    @Override
    public String toString() {
        return "Reservation{id='" + id + "', reservationNo='" + reservationNo + "', status='" + status + "'}";
    }
}

