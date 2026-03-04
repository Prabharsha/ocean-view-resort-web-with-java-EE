package com.oceanview.controller;

import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.service.GuestService;
import com.oceanview.service.ReservationService;
import com.oceanview.service.ServiceException;
import com.oceanview.service.impl.GuestServiceImpl;
import com.oceanview.service.impl.ReservationServiceImpl;
import com.oceanview.util.ServiceResult;
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

@WebServlet("/guests")
public class GuestController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(GuestController.class);

    private GuestService guestService;
    private ReservationService reservationService;

    @Override
    public void init() throws ServletException {
        guestService = new GuestServiceImpl();
        reservationService = new ReservationServiceImpl();
        log.info("GuestController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "list";
        log.debug("GET /guests action={}", action);
        try {
            switch (action) {
                case "list": handleList(req, resp); break;
                case "new": handleNewForm(req, resp); break;
                case "view": handleView(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /guests — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /guests action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/guest/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if (action == null) action = "create";
        log.debug("POST /guests action={}", action);
        try {
            switch (action) {
                case "create": handleCreate(req, resp); break;
                case "update": handleUpdate(req, resp); break;
                default:
                    log.warn("Unknown POST action '{}' for /guests — falling back to list", action);
                    handleList(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in POST /guests action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/guest/list.jsp").forward(req, resp);
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String keyword = req.getParameter("search");
        log.debug("handleList: search='{}'", keyword);
        List<Guest> guests;
        if (keyword != null && !keyword.trim().isEmpty()) {
            guests = guestService.searchGuests(keyword);
        } else {
            guests = guestService.getAllGuests();
        }
        log.debug("handleList: loaded {} guest(s)", guests.size());
        req.setAttribute("guests", guests);
        req.getRequestDispatcher("/WEB-INF/views/guest/list.jsp").forward(req, resp);
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        log.debug("handleView: id={}", id);
        Guest guest = guestService.getGuestById(id);
        if (guest == null) {
            log.warn("handleView: guest not found for id={}", id);
            resp.sendError(404, "Guest not found");
            return;
        }
        List<Reservation> reservations = reservationService.getReservationsByGuest(id);
        log.debug("handleView: guest={} has {} reservation(s)", guest.getName(), reservations.size());
        req.setAttribute("guest", guest);
        req.setAttribute("reservations", reservations);
        req.getRequestDispatcher("/WEB-INF/views/guest/list.jsp").forward(req, resp);
    }

    private void handleNewForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("handleNewForm: showing add guest form");
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/guest/add.jsp").forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Guest guest = new Guest();
        guest.setName(req.getParameter("name"));
        guest.setAddress(req.getParameter("address"));
        guest.setContact(req.getParameter("contact"));
        guest.setEmail(req.getParameter("email"));
        guest.setNic(req.getParameter("nic"));

        log.info("Creating guest: name='{}' email='{}' nic='{}'", guest.getName(), guest.getEmail(), guest.getNic());
        ServiceResult result = guestService.addGuest(guest);
        if (result.isSuccess()) {
            log.info("Guest created successfully: name='{}' id={}", guest.getName(), guest.getId());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/guests?action=list");
        } else {
            log.warn("Guest creation failed for name='{}': {}", guest.getName(), result.getMessage());
            req.setAttribute("error", result.getMessage());
            req.setAttribute("guest", guest);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/guest/add.jsp").forward(req, resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Guest guest = new Guest();
        guest.setId(req.getParameter("id"));
        guest.setName(req.getParameter("name"));
        guest.setAddress(req.getParameter("address"));
        guest.setContact(req.getParameter("contact"));
        guest.setEmail(req.getParameter("email"));
        guest.setNic(req.getParameter("nic"));

        log.info("Updating guest id={} name='{}'", guest.getId(), guest.getName());
        ServiceResult result = guestService.updateGuest(guest);
        if (result.isSuccess()) {
            log.info("Guest updated successfully: id={}", guest.getId());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/guests?action=list");
        } else {
            log.warn("Guest update failed for id='{}': {}", guest.getId(), result.getMessage());
            req.setAttribute("error", result.getMessage());
            req.setAttribute("guest", guest);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/guest/add.jsp").forward(req, resp);
        }
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

