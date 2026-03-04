package com.oceanview.filter;

import com.oceanview.model.User;
import com.oceanview.util.DBConnection;
import com.oceanview.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class LoggingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        long start = System.currentTimeMillis();
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String ip = req.getRemoteAddr();

        log.debug(">> {} {} {} [IP: {}]", method, uri, query != null ? "?" + query : "", ip);

        chain.doFilter(request, response);

        // Skip post-processing for async requests (e.g. SSE streams) — the
        // response is still open; timing and status are not yet final.
        if (request.isAsyncStarted()) {
            return;
        }

        long elapsed = System.currentTimeMillis() - start;
        int status = resp.getStatus();
        log.debug("<< {} {} -> HTTP {} ({}ms)", method, uri, status, elapsed);

        // Warn on slow requests
        if (elapsed > 3000) {
            log.warn("SLOW REQUEST: {} {} took {}ms", method, uri, elapsed);
        }

        // Audit log to DB for authenticated users
        User user = SessionUtil.getLoggedUser(req);
        if (user != null) {
            try {
                Connection conn = DBConnection.getInstance().getConnection();
                String sql = "INSERT INTO audit_log (id, user_id, action, entity, details, ip_address) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, user.getId());
                    ps.setString(3, method + " " + uri);
                    ps.setString(4, "HTTP_REQUEST");
                    ps.setString(5, query);
                    ps.setString(6, ip);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                log.error("Audit DB logging failed for user '{}' on {} {}: {}", user.getUsername(), method, uri, e.getMessage(), e);
            }
        }
    }

    @Override
    public void destroy() {}
}

