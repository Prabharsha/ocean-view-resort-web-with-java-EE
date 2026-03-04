package com.oceanview.controller;

import com.oceanview.model.Guest;
import com.oceanview.model.Payment;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.model.User;
import com.oceanview.service.*;
import com.oceanview.service.impl.*;
import com.oceanview.util.ServiceResult;
import com.oceanview.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@WebServlet("/reservations")
public class ReservationController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private ReservationService reservationService;
    private RoomService roomService;
    private GuestService guestService;
    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        reservationService = new ReservationServiceImpl();
        roomService = new RoomServiceImpl();
        guestService = new GuestServiceImpl();
        paymentService = new PaymentServiceImpl();
        log.info("ReservationController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        log.debug("GET /reservations action={} user={}", action, SessionUtil.getLoggedUser(req) != null ? SessionUtil.getLoggedUser(req).getUsername() : "anonymous");
        try {
            switch (action) {
                case "list": handleList(req, resp); break;
                case "view": handleView(req, resp); break;
                case "new": handleNewForm(req, resp); break;
                case "edit": handleEditForm(req, resp); break;
                case "bill": handleBill(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /reservations — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /reservations action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/reservation/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if (action == null) action = "create";
        log.debug("POST /reservations action={} user={}", action, SessionUtil.getLoggedUser(req) != null ? SessionUtil.getLoggedUser(req).getUsername() : "anonymous");
        try {
            switch (action) {
                case "create": handleCreate(req, resp); break;
                case "update": handleUpdate(req, resp); break;
                case "confirm": handleConfirm(req, resp); break;
                case "cancel": handleCancel(req, resp); break;
                case "checkin": handleCheckIn(req, resp); break;
                case "checkout": handleCheckOut(req, resp); break;
                default:
                    log.warn("Unknown POST action '{}' for /reservations — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in POST /reservations action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/reservation/list.jsp").forward(req, resp);
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String status = req.getParameter("status");
        log.debug("handleList: status filter='{}'", status);
        List<Reservation> reservations;
        if (status != null && !status.isEmpty()) {
            reservations = reservationService.getReservationsByStatus(status);
        } else {
            reservations = reservationService.getAllReservations();
        }
        // Enrich with guest name and room number for display
        for (Reservation r : reservations) {
            try {
                com.oceanview.model.Guest g = guestService.getGuestById(r.getGuestId());
                if (g != null) r.setGuestName(g.getName());
            } catch (Exception ignored) {}
            try {
                com.oceanview.model.Room rm = roomService.getRoom(r.getRoomId());
                if (rm != null) r.setRoomNumber(rm.getRoomNumber());
            } catch (Exception ignored) {}
        }
        log.debug("handleList: loaded {} reservation(s)", reservations.size());
        req.setAttribute("reservations", reservations);
        req.getRequestDispatcher("/WEB-INF/views/reservation/list.jsp").forward(req, resp);
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleView: id={}", id);
        Reservation reservation = reservationService.getReservation(id);
        if (reservation == null) {
            log.warn("handleView: reservation not found for id={}", id);
            resp.sendError(404, "Reservation not found");
            return;
        }
        Guest guest = guestService.getGuestById(reservation.getGuestId());
        Room room = roomService.getRoom(reservation.getRoomId());
        log.debug("handleView: reservation={} guest={} room={}", reservation.getReservationNo(),
                guest != null ? guest.getName() : "null", room != null ? room.getRoomNumber() : "null");
        req.setAttribute("reservation", reservation);
        req.setAttribute("guest", guest);
        req.setAttribute("room", room);
        // Load payment so view.jsp can render correct action buttons
        try {
            Payment payment = paymentService.getPaymentByReservation(id);
            req.setAttribute("payment", payment);
        } catch (ServiceException e) {
            log.warn("handleView: could not load payment for reservationId={}: {}", id, e.getMessage());
        }
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/reservation/view.jsp").forward(req, resp);
    }

    private void handleNewForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        List<Room> rooms = roomService.getAvailableRooms();
        List<Guest> guests = guestService.getAllGuests();
        log.debug("handleNewForm: availableRooms={} guests={}", rooms.size(), guests.size());
        req.setAttribute("rooms", rooms);
        req.setAttribute("guests", guests);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/reservation/add.jsp").forward(req, resp);
    }

    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleEditForm: id={}", id);
        Reservation reservation = reservationService.getReservation(id);
        if (reservation == null) {
            log.warn("handleEditForm: reservation not found for id={}", id);
            resp.sendError(404, "Reservation not found");
            return;
        }
        List<Room> rooms = roomService.getAllRooms();
        List<Guest> guests = guestService.getAllGuests();
        req.setAttribute("reservation", reservation);
        req.setAttribute("rooms", rooms);
        req.setAttribute("guests", guests);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/reservation/edit.jsp").forward(req, resp);
    }

    private void handleBill(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleBill: id={}", id);
        Reservation reservation = reservationService.getReservation(id);
        if (reservation == null) {
            log.warn("handleBill: reservation not found for id={}", id);
            resp.sendError(404, "Reservation not found");
            return;
        }
        Guest guest = guestService.getGuestById(reservation.getGuestId());
        Room room = roomService.getRoom(reservation.getRoomId());
        req.setAttribute("reservation", reservation);
        req.setAttribute("guest", guest);
        req.setAttribute("room", room);
        // Load payment so bill can show payment status
        try {
            Payment payment = paymentService.getPaymentByReservation(id);
            req.setAttribute("payment", payment);
        } catch (ServiceException e) {
            log.warn("handleBill: could not load payment for reservationId={}: {}", id, e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/reservation/bill.jsp").forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        User user = SessionUtil.getLoggedUser(req);
        Reservation r = new Reservation();
        r.setGuestId(req.getParameter("guestId"));
        r.setRoomId(req.getParameter("roomId"));
        String checkIn = req.getParameter("checkInDate");
        String checkOut = req.getParameter("checkOutDate");
        if (checkIn != null && !checkIn.isEmpty()) r.setCheckInDate(LocalDate.parse(checkIn));
        if (checkOut != null && !checkOut.isEmpty()) r.setCheckOutDate(LocalDate.parse(checkOut));
        String numGuests = req.getParameter("numGuests");
        r.setNumGuests(numGuests != null && !numGuests.isEmpty() ? Integer.parseInt(numGuests) : 1);
        r.setSpecialRequests(req.getParameter("specialRequests"));

        log.info("Creating reservation: guestId={} roomId={} checkIn={} checkOut={} numGuests={} by user={}",
                r.getGuestId(), r.getRoomId(), r.getCheckInDate(), r.getCheckOutDate(), r.getNumGuests(),
                user != null ? user.getUsername() : "unknown");

        ServiceResult result = reservationService.createReservation(r, user.getId());
        if (result.isSuccess()) {
            log.info("Reservation created successfully: {} by user={}", result.getMessage(), user.getUsername());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/reservations?action=list");
        } else {
            log.warn("Reservation creation failed: {} (guestId={} roomId={} by user={})",
                    result.getMessage(), r.getGuestId(), r.getRoomId(), user != null ? user.getUsername() : "unknown");
            req.setAttribute("error", result.getMessage());
            req.setAttribute("reservation", r);
            List<Room> rooms = roomService.getAvailableRooms();
            List<Guest> guests = guestService.getAllGuests();
            req.setAttribute("rooms", rooms);
            req.setAttribute("guests", guests);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/reservation/add.jsp").forward(req, resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Reservation r = new Reservation();
        r.setId(req.getParameter("id"));
        r.setGuestId(req.getParameter("guestId"));
        r.setRoomId(req.getParameter("roomId"));
        String checkIn = req.getParameter("checkInDate");
        String checkOut = req.getParameter("checkOutDate");
        if (checkIn != null && !checkIn.isEmpty()) r.setCheckInDate(LocalDate.parse(checkIn));
        if (checkOut != null && !checkOut.isEmpty()) r.setCheckOutDate(LocalDate.parse(checkOut));
        String numGuests = req.getParameter("numGuests");
        r.setNumGuests(numGuests != null && !numGuests.isEmpty() ? Integer.parseInt(numGuests) : 1);
        r.setSpecialRequests(req.getParameter("specialRequests"));
        r.setStatus(req.getParameter("status"));

        log.info("Updating reservation id={} status={} checkIn={} checkOut={}", r.getId(), r.getStatus(), r.getCheckInDate(), r.getCheckOutDate());
        ServiceResult result = reservationService.updateReservation(r);
        if (result.isSuccess()) {
            log.info("Reservation updated successfully: id={}", r.getId());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + r.getId());
        } else {
            log.warn("Reservation update failed for id={}: {}", r.getId(), result.getMessage());
            req.setAttribute("error", result.getMessage());
            req.setAttribute("reservation", r);
            List<Room> rooms = roomService.getAllRooms();
            List<Guest> guests = guestService.getAllGuests();
            req.setAttribute("rooms", rooms);
            req.setAttribute("guests", guests);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/reservation/edit.jsp").forward(req, resp);
        }
    }

    private void handleConfirm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = SessionUtil.getLoggedUser(req);
        log.info("Confirm reservation id={} by user={}", id, user != null ? user.getUsername() : "unknown");
        ServiceResult result = reservationService.confirmReservation(id);
        if (result.isSuccess()) {
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id);
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = SessionUtil.getLoggedUser(req);
        log.info("Cancelling reservation id={} by user={}", id, user != null ? user.getUsername() : "unknown");
        ServiceResult result = reservationService.cancelReservation(id);
        if (result.isSuccess()) {
            log.info("Reservation cancelled: id={}", id);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("Reservation cancel failed for id={}: {}", id, result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/reservations?action=list");
    }

    private void handleCheckIn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = SessionUtil.getLoggedUser(req);
        log.info("Check-in requested for reservationId={} by staff={}", id, user != null ? user.getUsername() : "unknown");

        // Enforce that payment exists before allowing check-in
        Payment existingPayment = null;
        try { existingPayment = paymentService.getPaymentByReservation(id); } catch (ServiceException ignored) {}
        if (existingPayment == null) {
            req.getSession().setAttribute("flashError", "Payment must be processed before checking in.");
            resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id);
            return;
        }

        ServiceResult result = reservationService.checkIn(id, user.getId());
        if (result.isSuccess()) {
            log.info("Check-in successful for reservationId={}", id);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("Check-in failed for reservationId={}: {}", id, result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id);
    }

    private void handleCheckOut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = SessionUtil.getLoggedUser(req);
        log.info("Check-out requested for reservationId={} by staff={}", id, user != null ? user.getUsername() : "unknown");
        ServiceResult result = reservationService.checkOut(id, user.getId());
        if (result.isSuccess()) {
            log.info("Check-out successful for reservationId={}", id);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("Check-out failed for reservationId={}: {}", id, result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id);
    }

    private void generateCsrfToken(HttpServletRequest req) {
        String token = UUID.randomUUID().toString();
        req.getSession().setAttribute("csrfToken", token);
        req.setAttribute("csrfToken", token);
    }

    private boolean validateCsrf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String formToken = req.getParameter("csrfToken");
        String sessionToken = (String) req.getSession().getAttribute("csrfToken");
        if (formToken == null || !formToken.equals(sessionToken)) {
            resp.sendError(403, "CSRF validation failed"); return false;
        }
        return true;
    }
}

