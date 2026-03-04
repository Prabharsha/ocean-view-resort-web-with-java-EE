package com.oceanview.util;

import com.oceanview.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;

public class SessionUtil {
    private static final String USER_KEY = "loggedUser";
    private static final int TIMEOUT = 30 * 60;

    public static void createSession(HttpServletRequest req, User user) {
        HttpSession s = req.getSession(true);
        s.setAttribute(USER_KEY, user);
        s.setAttribute("userRole", user.getRole());
        s.setAttribute("loginTime", LocalDateTime.now().toString());
        s.setMaxInactiveInterval(TIMEOUT);
    }

    public static User getLoggedUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null ? (User) s.getAttribute(USER_KEY) : null;
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getLoggedUser(req) != null;
    }

    public static boolean hasRole(HttpServletRequest req, String role) {
        User u = getLoggedUser(req);
        return u != null && role.equals(u.getRole());
    }

    public static void invalidate(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
    }
}

