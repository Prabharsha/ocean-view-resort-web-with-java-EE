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

@WebServlet("/profile")
public class ProfileController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getLoggedUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/auth?action=loginForm"); return; }
        generateCsrfToken(req);
        req.setAttribute("profileUser", user);
        req.getRequestDispatcher("/WEB-INF/views/profile/index.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if ("changePassword".equals(action)) {
            handleChangePassword(req, resp);
        } else if ("updateProfile".equals(action)) {
            handleUpdateProfile(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/profile");
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getLoggedUser(req);
        String current  = req.getParameter("currentPassword");
        String newPw    = req.getParameter("newPassword");
        String confirmPw = req.getParameter("confirmPassword");

        try {
            if (newPw != null && !newPw.equals(confirmPw)) {
                req.setAttribute("pwError", "New passwords do not match");
                forwardToProfile(req, resp, user);
                return;
            }
            ServiceResult result = userService.changePassword(user.getId(), current, newPw);
            if (result.isSuccess()) {
                req.getSession().setAttribute("flashSuccess", "Password changed successfully. Please log in again.");
                SessionUtil.invalidate(req);
                resp.sendRedirect(req.getContextPath() + "/auth?action=loginForm");
            } else {
                req.setAttribute("pwError", result.getMessage());
                forwardToProfile(req, resp, user);
            }
        } catch (ServiceException e) {
            req.setAttribute("pwError", e.getMessage());
            forwardToProfile(req, resp, user);
        }
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getLoggedUser(req);
        try {
            User updated = userService.getUserById(user.getId());
            if (updated == null) { resp.sendRedirect(req.getContextPath() + "/profile"); return; }
            updated.setFname(req.getParameter("fname"));
            updated.setLname(req.getParameter("lname"));
            updated.setEmail(req.getParameter("email"));
            updated.setPhone(req.getParameter("phone"));
            ServiceResult result = userService.updateUser(updated);
            if (result.isSuccess()) {
                // Refresh session user
                SessionUtil.createSession(req, updated);
                req.getSession().setAttribute("flashSuccess", "Profile updated successfully");
            } else {
                req.getSession().setAttribute("flashError", result.getMessage());
            }
        } catch (ServiceException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    private void forwardToProfile(HttpServletRequest req, HttpServletResponse resp, User user) throws ServletException, IOException {
        generateCsrfToken(req);
        req.setAttribute("profileUser", user);
        req.getRequestDispatcher("/WEB-INF/views/profile/index.jsp").forward(req, resp);
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

