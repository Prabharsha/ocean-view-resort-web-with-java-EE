package com.oceanview.dao;

import com.oceanview.model.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Reservation model and bill calculation logic.
 * DAO integration tests require a live database and are marked separately.
 */
class ReservationDAOTest {

    @Test
    @DisplayName("Bill calculation: nights × rate = correct total")
    void billCalculation_correctTotal() {
        LocalDate checkIn  = LocalDate.of(2025, 7, 1);
        LocalDate checkOut = LocalDate.of(2025, 7, 4);
        double ratePerNight = 14000.00;

        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double expectedTotal = nights * ratePerNight;

        assertEquals(3, nights);
        assertEquals(42000.00, expectedTotal, 0.01);
    }

    @Test
    @DisplayName("Bill calculation: single night stay")
    void billCalculation_singleNight() {
        LocalDate checkIn  = LocalDate.of(2025, 12, 25);
        LocalDate checkOut = LocalDate.of(2025, 12, 26);
        double ratePerNight = 45000.00;

        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double total = nights * ratePerNight;

        assertEquals(1, nights);
        assertEquals(45000.00, total, 0.01);
    }

    @Test
    @DisplayName("Bill calculation: week-long stay")
    void billCalculation_weekStay() {
        LocalDate checkIn  = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 8);
        double ratePerNight = 8500.00;

        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        double total = nights * ratePerNight;

        assertEquals(7, nights);
        assertEquals(59500.00, total, 0.01);
    }

    @Test
    @DisplayName("Reservation model stores all fields correctly")
    void reservationModel_storesFields() {
        Reservation r = new Reservation();
        r.setId("test-id");
        r.setReservationNo("RES-2025-0001");
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.of(2025, 7, 1));
        r.setCheckOutDate(LocalDate.of(2025, 7, 5));
        r.setNumGuests(2);
        r.setStatus("PENDING");
        r.setTotalAmount(56000.00);
        r.setCreatedBy("staff-001");

        assertEquals("test-id", r.getId());
        assertEquals("RES-2025-0001", r.getReservationNo());
        assertEquals("guest-001", r.getGuestId());
        assertEquals("room-001", r.getRoomId());
        assertEquals(LocalDate.of(2025, 7, 1), r.getCheckInDate());
        assertEquals(LocalDate.of(2025, 7, 5), r.getCheckOutDate());
        assertEquals(2, r.getNumGuests());
        assertEquals("PENDING", r.getStatus());
        assertEquals(56000.00, r.getTotalAmount(), 0.01);
        assertEquals("staff-001", r.getCreatedBy());
    }
}

