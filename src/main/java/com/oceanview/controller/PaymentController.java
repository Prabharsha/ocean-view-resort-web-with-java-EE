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
import java.util.List;
import java.util.UUID;

@WebServlet("/payments")
public class PaymentController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private PaymentService paymentService;
    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentServiceImpl();
        reservationService = new ReservationServiceImpl();
        guestService = new GuestServiceImpl();
        roomService = new RoomServiceImpl();
        log.info("PaymentController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        log.debug("GET /payments action={}", action);
        try {
            switch (action) {
                case "form": handleForm(req, resp); break;
                case "receipt": handleReceipt(req, resp); break;
                case "list": handleList(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /payments — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /payments action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/payment/form.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if (action == null) action = "process";
        log.debug("POST /payments action={}", action);
        try {
            if ("process".equals(action)) {
                handleProcess(req, resp);
            } else {
                log.warn("Unknown POST action '{}' for /payments", action);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in POST /payments action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/payment/form.jsp").forward(req, resp);
        }
    }

    private void handleForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String reservationId = req.getParameter("reservationId");
        log.debug("handleForm: reservationId={}", reservationId);
        Reservation reservation = reservationService.getReservation(reservationId);
        if (reservation == null) {
            log.warn("handleForm: reservation not found for id={}", reservationId);
            resp.sendError(404, "Reservation not found");
            return;
        }

        Guest guest = guestService.getGuestById(reservation.getGuestId());
        Room room = roomService.getRoom(reservation.getRoomId());
        boolean alreadyPaid = paymentService.isPaymentExists(reservationId);
        log.debug("handleForm: reservationNo={} alreadyPaid={}", reservation.getReservationNo(), alreadyPaid);

        req.setAttribute("reservation", reservation);
        req.setAttribute("guest", guest);
        req.setAttribute("room", room);
        req.setAttribute("alreadyPaid", alreadyPaid);

        if (alreadyPaid) {
            Payment existingPayment = paymentService.getPaymentByReservation(reservationId);
            req.setAttribute("existingPayment", existingPayment);
        }

        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/payment/form.jsp").forward(req, resp);
    }

    private void handleProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String reservationId = req.getParameter("reservationId");
        String paymentMethod = req.getParameter("paymentMethod");
        String referenceNo = req.getParameter("referenceNo");
        String notes = req.getParameter("notes");
        User user = SessionUtil.getLoggedUser(req);

        log.info("Processing payment: reservationId={} method={} referenceNo={} by staff={}",
                reservationId, paymentMethod, referenceNo, user != null ? user.getUsername() : "unknown");

        ServiceResult result = paymentService.processPayment(reservationId, paymentMethod, referenceNo, notes, user.getId());
        if (result.isSuccess()) {
            Payment payment = (Payment) result.getData();
            log.info("Payment processed successfully: id={} amount={} reservationId={}", payment.getId(), payment.getAmount(), reservationId);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/payments?action=receipt&id=" + payment.getId());
        } else {
            log.warn("Payment processing failed for reservationId='{}': {}", reservationId, result.getMessage());
            req.setAttribute("error", result.getMessage());
            // Reload form data
            Reservation reservation = reservationService.getReservation(reservationId);
            if (reservation != null) {
                Guest guest = guestService.getGuestById(reservation.getGuestId());
                Room room = roomService.getRoom(reservation.getRoomId());
                req.setAttribute("reservation", reservation);
                req.setAttribute("guest", guest);
                req.setAttribute("room", room);
                req.setAttribute("alreadyPaid", false);
            }
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/payment/form.jsp").forward(req, resp);
        }
    }

    private void handleReceipt(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleReceipt: paymentId={}", id);
        Payment payment = paymentService.getPayment(id);
        if (payment == null) {
            log.warn("handleReceipt: payment not found for id={}", id);
            resp.sendError(404, "Payment not found");
            return;
        }

        Reservation reservation = reservationService.getReservation(payment.getReservationId());
        Guest guest = guestService.getGuestById(reservation.getGuestId());
        Room room = roomService.getRoom(reservation.getRoomId());
        log.debug("handleReceipt: payment={} reservation={} guest={}", id, reservation.getReservationNo(), guest != null ? guest.getName() : "null");

        req.setAttribute("payment", payment);
        req.setAttribute("reservation", reservation);
        req.setAttribute("guest", guest);
        req.setAttribute("room", room);
        req.getRequestDispatcher("/WEB-INF/views/payment/receipt.jsp").forward(req, resp);
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        List<Payment> payments = paymentService.getAllPayments();
        log.debug("handleList: loaded {} payment(s)", payments.size());
        req.setAttribute("payments", payments);
        req.getRequestDispatcher("/WEB-INF/views/payment/form.jsp").forward(req, resp);
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
            log.warn("CSRF validation failed from IP={}", req.getRemoteAddr());
            resp.sendError(403, "CSRF validation failed"); return false;
        }
        return true;
    }
}

