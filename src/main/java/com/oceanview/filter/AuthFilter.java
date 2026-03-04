package com.oceanview.filter;

import com.oceanview.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {"/auth", "/public/", "/index.jsp"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        // Allow public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath) || path.equals("/") || path.isEmpty()) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Check if user is logged in
        if (!SessionUtil.isLoggedIn(req)) {
            resp.sendRedirect(contextPath + "/auth?action=loginForm");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

