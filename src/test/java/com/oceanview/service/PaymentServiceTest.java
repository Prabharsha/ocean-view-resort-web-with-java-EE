package com.oceanview.service;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.PaymentDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Guest;
import com.oceanview.model.Payment;
import com.oceanview.model.Reservation;
import com.oceanview.service.impl.PaymentServiceImpl;
import com.oceanview.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock private PaymentDAO paymentDAO;
    @Mock private ReservationDAO reservationDAO;
    @Mock private GuestDAO guestDAO;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentServiceImpl(paymentDAO, reservationDAO, guestDAO);
    }

    private Reservation createValidReservation() {
        Reservation r = new Reservation();
        r.setId("res-001");
        r.setReservationNo("RES-2025-0001");
        r.setGuestId("guest-001");
        r.setStatus("PENDING");
        r.setTotalAmount(25500.00);
        return r;
    }

    @Test
    @DisplayName("Should fail when reservation not found")
    void processPayment_reservationNotFound_fails() throws Exception {
        when(reservationDAO.findById("nonexistent")).thenReturn(null);

        ServiceResult result = paymentService.processPayment("nonexistent", "CASH", "REC-001", "", "staff-001");
        assertFalse(result.isSuccess());
        assertEquals("Reservation not found", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when reservation is CANCELLED")
    void processPayment_cancelled_fails() throws Exception {
        Reservation r = createValidReservation();
        r.setStatus("CANCELLED");
        when(reservationDAO.findById("res-001")).thenReturn(r);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "REC-001", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CANCELLED"));
    }

    @Test
    @DisplayName("Should fail when reservation is CHECKED_OUT")
    void processPayment_checkedOut_fails() throws Exception {
        Reservation r = createValidReservation();
        r.setStatus("CHECKED_OUT");
        when(reservationDAO.findById("res-001")).thenReturn(r);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "REC-001", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("CHECKED_OUT"));
    }

    @Test
    @DisplayName("Should fail when duplicate payment exists")
    void processPayment_duplicate_fails() throws Exception {
        Reservation r = createValidReservation();
        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(true);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "REC-001", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already been recorded"));
    }

    @Test
    @DisplayName("Should fail when invalid payment method")
    void processPayment_invalidMethod_fails() throws Exception {
        Reservation r = createValidReservation();
        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);

        ServiceResult result = paymentService.processPayment("res-001", "BITCOIN", "REF-001", "", "staff-001");
        assertFalse(result.isSuccess());
        assertEquals("Payment method must be CASH or CARD", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when reference number is empty")
    void processPayment_emptyReference_fails() throws Exception {
        Reservation r = createValidReservation();
        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Reference number is required"));
    }

    @Test
    @DisplayName("Should fail when cash receipt number is too short")
    void processPayment_shortCashRef_fails() throws Exception {
        Reservation r = createValidReservation();
        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "AB", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Cash receipt number must be at least 3 characters"));
    }

    @Test
    @DisplayName("Should fail when card transaction ID is too short")
    void processPayment_shortCardRef_fails() throws Exception {
        Reservation r = createValidReservation();
        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);

        ServiceResult result = paymentService.processPayment("res-001", "CARD", "ABC", "", "staff-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Card transaction ID must be at least 6 characters"));
    }

    @Test
    @DisplayName("Should succeed with valid cash payment")
    void processPayment_validCash_succeeds() throws Exception {
        Reservation r = createValidReservation();
        Guest g = new Guest();
        g.setId("guest-001");
        g.setName("Test Guest");
        g.setEmail("test@test.com");

        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);
        when(guestDAO.findById("guest-001")).thenReturn(g);

        ServiceResult result = paymentService.processPayment("res-001", "CASH", "REC-12345", "Cash payment", "staff-001");
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("recorded successfully"));
        verify(paymentDAO).save(any(Payment.class));
        verify(reservationDAO).updateStatus("res-001", "CONFIRMED");
    }

    @Test
    @DisplayName("Should succeed with valid card payment")
    void processPayment_validCard_succeeds() throws Exception {
        Reservation r = createValidReservation();
        Guest g = new Guest();
        g.setId("guest-001");
        g.setName("Test Guest");
        g.setEmail("test@test.com");

        when(reservationDAO.findById("res-001")).thenReturn(r);
        when(paymentDAO.existsForReservation("res-001")).thenReturn(false);
        when(guestDAO.findById("guest-001")).thenReturn(g);

        ServiceResult result = paymentService.processPayment("res-001", "CARD", "TXN-123456", "", "staff-001");
        assertTrue(result.isSuccess());
        verify(paymentDAO).save(any(Payment.class));
    }
}

