package com.oceanview.controller;

import com.oceanview.model.Room;
import com.oceanview.model.User;
import com.oceanview.service.RoomService;
import com.oceanview.service.ServiceException;
import com.oceanview.service.UserService;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.service.impl.UserServiceImpl;
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

@WebServlet("/maintenance")
public class MaintenanceController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceController.class);

    private RoomService roomService;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        roomService = new RoomServiceImpl();
        userService = new UserServiceImpl();
        log.info("MaintenanceController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.hasRole(req, "ADMIN")) {
            log.warn("Access denied to /maintenance GET — user={} role={}",
                    SessionUtil.getLoggedUser(req) != null ? SessionUtil.getLoggedUser(req).getUsername() : "unknown",
                    SessionUtil.getLoggedUser(req) != null ? SessionUtil.getLoggedUser(req).getRole() : "none");
            resp.sendError(403, "Access denied — Admin only");
            return;
        }
        String action = req.getParameter("action");
        if (action == null) action = "rooms";
        log.debug("GET /maintenance action={}", action);
        try {
            switch (action) {
                case "rooms": handleRoomMaintenance(req, resp); break;
                case "users": handleUserList(req, resp); break;
                case "userForm": handleUserForm(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /maintenance — falling back to rooms", action);
                    handleRoomMaintenance(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /maintenance action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/room/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.hasRole(req, "ADMIN")) {
            log.warn("Access denied to /maintenance POST");
            resp.sendError(403);
            return;
        }
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        log.debug("POST /maintenance action={}", action);
        try {
            switch (action) {
                case "toggleRoom": handleToggleRoom(req, resp); break;
                case "createUser": handleCreateUser(req, resp); break;
                case "deleteUser": handleDeleteUser(req, resp); break;
                default:
                    log.warn("Unknown POST action '{}' for /maintenance", action);
                    resp.sendRedirect(req.getContextPath() + "/maintenance");
            }
        } catch (ServiceException e) {
            log.error("ServiceException in POST /maintenance action='{}': {}", action, e.getMessage(), e);
            req.getSession().setAttribute("flashError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/maintenance");
        }
    }

    private void handleRoomMaintenance(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        List<Room> rooms = roomService.getAllRooms();
        log.debug("handleRoomMaintenance: loaded {} room(s)", rooms.size());
        req.setAttribute("rooms", rooms);
        req.setAttribute("maintenanceView", true);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/room/list.jsp").forward(req, resp);
    }

    private void handleToggleRoom(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String roomId = req.getParameter("roomId");
        log.info("Toggling room availability: roomId={}", roomId);
        ServiceResult result = roomService.toggleMaintenance(roomId);
        log.info("Toggle result for roomId={}: {}", roomId, result.getMessage());
        req.getSession().setAttribute("flashSuccess", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/maintenance?action=rooms");
    }

    private void handleUserList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        List<User> users = userService.getAllUsers();
        log.debug("handleUserList: loaded {} user(s)", users.size());
        req.setAttribute("users", users);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/dashboard/index.jsp").forward(req, resp);
    }

    private void handleUserForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("handleUserForm: showing user registration form");
        generateCsrfToken(req);
        req.setAttribute("showRegister", true);
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    private void handleCreateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        User user = new User();
        user.setFname(req.getParameter("fname"));
        user.setLname(req.getParameter("lname"));
        user.setUsername(req.getParameter("username"));
        user.setPassword(req.getParameter("password"));
        user.setEmail(req.getParameter("email"));
        user.setPhone(req.getParameter("phone"));
        user.setRole(req.getParameter("role"));

        log.info("Creating user: username='{}' role='{}' email='{}'", user.getUsername(), user.getRole(), user.getEmail());
        ServiceResult result = userService.register(user);
        if (result.isSuccess()) {
            log.info("User created by admin: username='{}' role='{}'", user.getUsername(), user.getRole());
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("User creation failed for username='{}': {}", user.getUsername(), result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/maintenance?action=users");
    }

    private void handleDeleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String userId = req.getParameter("userId");
        log.info("Deleting user id={}", userId);
        ServiceResult result = userService.deleteUser(userId);
        if (result.isSuccess()) {
            log.info("User deleted: id={}", userId);
            req.getSession().setAttribute("flashSuccess", result.getMessage());
        } else {
            log.warn("User deletion failed for id='{}': {}", userId, result.getMessage());
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/maintenance?action=users");
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
            log.warn("CSRF validation failed in /maintenance from IP={}", req.getRemoteAddr());
            resp.sendError(403, "CSRF validation failed"); return false;
        }
        return true;
    }
}

