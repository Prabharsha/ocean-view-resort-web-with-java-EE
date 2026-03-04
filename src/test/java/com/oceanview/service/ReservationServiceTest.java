package com.oceanview.service;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.service.impl.ReservationServiceImpl;
import com.oceanview.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock private ReservationDAO reservationDAO;
    @Mock private RoomDAO roomDAO;
    @Mock private GuestDAO guestDAO;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationServiceImpl(reservationDAO, roomDAO, guestDAO);
    }

    @Test
    @DisplayName("Should fail when guest ID is empty")
    void createReservation_noGuest_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(1);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Guest is required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when room ID is empty")
    void createReservation_noRoom_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("");
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(1);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Room is required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when check-in date is null")
    void createReservation_noCheckIn_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(null);
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(1);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Check-in date required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when check-out is before check-in")
    void createReservation_invalidDates_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.now().plusDays(5));
        r.setCheckOutDate(LocalDate.now().plusDays(2));
        r.setNumGuests(1);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Check-out must be after check-in", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when check-in is in the past")
    void createReservation_pastCheckIn_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.now().minusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(2));
        r.setNumGuests(1);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Check-in cannot be in the past", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when guest count less than 1")
    void createReservation_zeroGuests_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(0);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("At least 1 guest required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when room not found")
    void createReservation_roomNotFound_fails() throws Exception {
        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("nonexistent");
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(1);

        when(roomDAO.findById("nonexistent")).thenReturn(null);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertEquals("Room not found", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when guest count exceeds room capacity")
    void createReservation_capacityExceeded_fails() throws Exception {
        Room room = new Room();
        room.setId("room-001");
        room.setCapacity(2);

        Reservation r = new Reservation();
        r.setGuestId("guest-001");
        r.setRoomId("room-001");
        r.setCheckInDate(LocalDate.now().plusDays(1));
        r.setCheckOutDate(LocalDate.now().plusDays(3));
        r.setNumGuests(5);

        when(roomDAO.findById("room-001")).thenReturn(room);

        ServiceResult result = reservationService.createReservation(r, "user-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Exceeds room capacity"));
    }

    @Test
    @DisplayName("Should fail when cancelling a checked-out reservation")
    void cancelReservation_checkedOut_fails() throws Exception {
        Reservation r = new Reservation();
        r.setId("res-001");
        r.setStatus("CHECKED_OUT");
        when(reservationDAO.findById("res-001")).thenReturn(r);

        ServiceResult result = reservationService.cancelReservation("res-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Cannot cancel"));
    }

    @Test
    @DisplayName("Should fail when checking in non-CONFIRMED reservation")
    void checkIn_notConfirmed_fails() throws Exception {
        Reservation r = new Reservation();
        r.setId("res-001");
        r.setStatus("PENDING");
        when(reservationDAO.findById("res-001")).thenReturn(r);

        ServiceResult result = reservationService.checkIn("res-001", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CONFIRMED"));
    }
}

