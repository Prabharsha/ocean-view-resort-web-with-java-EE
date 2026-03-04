package com.oceanview.controller;

import com.oceanview.model.User;
import com.oceanview.service.ServiceException;
import com.oceanview.service.UserService;
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
import java.util.UUID;

@WebServlet("/auth")
public class AuthController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "loginForm";

        switch (action) {
            case "loginForm": showLoginForm(req, resp); break;
            case "logout": handleLogout(req, resp); break;
            case "registerForm": showRegisterForm(req, resp); break;
            case "resetForm": showResetForm(req, resp); break;
            default: showLoginForm(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null) action = "login";

        switch (action) {
            case "login": handleLogin(req, resp); break;
            case "register": handleRegister(req, resp); break;
            case "resetPassword": handleResetPassword(req, resp); break;
            default: handleLogin(req, resp);
        }
    }

    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    private void showRegisterForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    private void showResetForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        log.debug("Login attempt for username: {}", username);
        try {
            ServiceResult result = userService.authenticate(username, password);
            if (result.isSuccess()) {
                User user = (User) result.getData();
                SessionUtil.createSession(req, user);
                log.info("Login successful for user: {} [role={}] from IP: {}", username, user.getRole(), req.getRemoteAddr());
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                log.warn("Login failed for username: '{}' from IP: {} - reason: {}", username, req.getRemoteAddr(), result.getMessage());
                req.setAttribute("error", result.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            }
        } catch (ServiceException e) {
            log.error("Login threw exception for username '{}': {}", username, e.getMessage(), e);
            req.setAttribute("error", "An error occurred during login. Please try again.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        if (!SessionUtil.hasRole(req, "ADMIN")) { resp.sendError(403, "Access denied"); return; }

        User user = new User();
        user.setFname(req.getParameter("fname"));
        user.setLname(req.getParameter("lname"));
        user.setUsername(req.getParameter("username"));
        user.setPassword(req.getParameter("password"));
        user.setEmail(req.getParameter("email"));
        user.setPhone(req.getParameter("phone"));
        user.setRole(req.getParameter("role"));

        try {
            ServiceResult result = userService.register(user);
            if (result.isSuccess()) {
                log.info("New user registered: {} [role={}] by admin from IP: {}", user.getUsername(), user.getRole(), req.getRemoteAddr());
                req.getSession().setAttribute("flashSuccess", "User registered successfully");
                resp.sendRedirect(req.getContextPath() + "/maintenance?action=users");
            } else {
                log.warn("User registration failed for '{}': {}", user.getUsername(), result.getMessage());
                req.setAttribute("error", result.getMessage());
                req.setAttribute("user", user);
                generateCsrfToken(req);
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            }
        } catch (ServiceException e) {
            log.error("User registration threw exception for '{}': {}", user.getUsername(), e.getMessage(), e);
            req.setAttribute("error", "Registration failed. Please try again.");
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String userId = req.getParameter("userId");
        String newPassword = req.getParameter("newPassword");
        log.debug("Password reset requested for userId: {}", userId);
        try {
            ServiceResult result = userService.resetPassword(userId, newPassword);
            if (result.isSuccess()) {
                log.info("Password reset successfully for userId: {}", userId);
                req.getSession().setAttribute("flashSuccess", "Password reset successfully");
                resp.sendRedirect(req.getContextPath() + "/maintenance?action=users");
            } else {
                log.warn("Password reset failed for userId '{}': {}", userId, result.getMessage());
                req.setAttribute("error", result.getMessage());
                generateCsrfToken(req);
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            }
        } catch (ServiceException e) {
            log.error("Password reset threw exception for userId '{}': {}", userId, e.getMessage(), e);
            req.setAttribute("error", "Password reset failed.");
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = SessionUtil.getLoggedUser(req) != null ? SessionUtil.getLoggedUser(req).getUsername() : "unknown";
        SessionUtil.invalidate(req);
        log.info("User '{}' logged out from IP: {}", username, req.getRemoteAddr());
        resp.sendRedirect(req.getContextPath() + "/auth?action=loginForm");
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
            resp.sendError(403, "CSRF validation failed");
            return false;
        }
        return true;
    }
}

