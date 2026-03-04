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
import java.util.List;
import java.util.UUID;

@WebServlet("/staff")
public class StaffController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(StaffController.class);
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.hasRole(req, "ADMIN")) { resp.sendError(403, "Access denied"); return; }
        String action = req.getParameter("action");
        if (action == null) action = "list";
        try {
            switch (action) {
                case "list": handleList(req, resp); break;
                case "new":  handleNewForm(req, resp); break;
                case "edit": handleEditForm(req, resp); break;
                default: handleList(req, resp); break;
            }
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            try { handleList(req, resp); } catch (ServiceException ex) { req.setAttribute("staffUsers", java.util.Collections.emptyList()); req.getRequestDispatcher("/WEB-INF/views/staff/list.jsp").forward(req, resp); }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.hasRole(req, "ADMIN")) { resp.sendError(403, "Access denied"); return; }
        if (!validateCsrf(req, resp)) return;
        String action = req.getParameter("action");
        if (action == null) action = "create";
        try {
            switch (action) {
                case "create": handleCreate(req, resp); break;
                case "update": handleUpdate(req, resp); break;
                case "delete": handleDelete(req, resp); break;
                case "toggle": handleToggle(req, resp); break;
                case "resetPassword": handleResetPassword(req, resp); break;
                default: handleList(req, resp);
            }
        } catch (ServiceException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/staff");
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        List<User> users = userService.getAllUsers();
        req.setAttribute("staffUsers", users);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/staff/list.jsp").forward(req, resp);
    }

    private void handleNewForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/staff/form.jsp").forward(req, resp);
    }

    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = userService.getUserById(id);
        if (user == null) { resp.sendError(404, "User not found"); return; }
        req.setAttribute("editUser", user);
        generateCsrfToken(req);
        req.getRequestDispatcher("/WEB-INF/views/staff/form.jsp").forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        User user = buildUserFromRequest(req);
        ServiceResult result = userService.createStaffUser(user);
        if (result.isSuccess()) {
            req.getSession().setAttribute("flashSuccess", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/staff");
        } else {
            req.setAttribute("error", result.getMessage());
            req.setAttribute("editUser", user);
            generateCsrfToken(req);
            req.getRequestDispatcher("/WEB-INF/views/staff/form.jsp").forward(req, resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String id = req.getParameter("id");
        User user = userService.getUserById(id);
        if (user == null) { resp.sendError(404, "User not found"); return; }
        user.setFname(req.getParameter("fname"));
        user.setLname(req.getParameter("lname"));
        user.setEmail(req.getParameter("email"));
        user.setPhone(req.getParameter("phone"));
        user.setRole(req.getParameter("role"));
        ServiceResult result = userService.updateUser(user);
        if (result.isSuccess()) {
            req.getSession().setAttribute("flashSuccess", "Staff user updated successfully");
        } else {
            req.getSession().setAttribute("flashError", result.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/staff");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String id = req.getParameter("id");
        User self = SessionUtil.getLoggedUser(req);
        if (self != null && self.getId().equals(id)) {
            req.getSession().setAttribute("flashError", "You cannot delete your own account");
            resp.sendRedirect(req.getContextPath() + "/staff"); return;
        }
        ServiceResult result = userService.deleteUser(id);
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/staff");
    }

    private void handleToggle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String id = req.getParameter("id");
        ServiceResult result = userService.toggleUserActive(id);
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/staff");
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String id = req.getParameter("id");
        String newPw = req.getParameter("newPassword");
        ServiceResult result = userService.resetPassword(id, newPw);
        req.getSession().setAttribute(result.isSuccess() ? "flashSuccess" : "flashError", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/staff");
    }

    private User buildUserFromRequest(HttpServletRequest req) {
        User u = new User();
        u.setFname(req.getParameter("fname"));
        u.setLname(req.getParameter("lname"));
        u.setUsername(req.getParameter("username"));
        u.setEmail(req.getParameter("email"));
        u.setPhone(req.getParameter("phone"));
        u.setRole(req.getParameter("role"));
        return u;
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

