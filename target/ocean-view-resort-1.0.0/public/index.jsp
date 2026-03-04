<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ocean View Resort — Galle, Sri Lanka</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <style>
        .landing { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%); }
        .landing-card { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); padding: 48px; text-align: center; max-width: 480px; width: 90%; }
        .landing-icon { font-size: 64px; margin-bottom: 16px; }
        .landing-card h1 { color: var(--primary); font-size: 28px; margin-bottom: 8px; }
        .landing-card p { color: var(--text-muted); margin-bottom: 24px; line-height: 1.6; }
        .landing-card .subtitle { font-size: 14px; color: var(--text-muted); margin-bottom: 32px; }
        .landing-actions { display: flex; gap: 12px; justify-content: center; }
    </style>
</head>
<body>
    <div class="landing">
        <div class="landing-card">
            <div class="landing-icon">&#127754;</div>
            <h1>Ocean View Resort</h1>
            <p class="subtitle">Beachside Hotel — Galle, Sri Lanka</p>
            <p>Premium room reservation management system for staff and administrators.</p>
            <div class="landing-actions">
                <a href="${ctx}/auth?action=loginForm" class="btn btn-primary">Staff Login</a>
            </div>
            <p style="margin-top: 32px; font-size: 12px; color: var(--text-muted);">&copy; 2025 Ocean View Resort. All rights reserved.</p>
        </div>
    </div>
</body>
</html>

