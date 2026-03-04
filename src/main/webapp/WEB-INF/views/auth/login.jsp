Reservations<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Ocean View Resort</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
</head>
<body class="login-page">
    <div class="login-container">
        <div class="login-card">
            <div class="login-header">
                <span class="login-icon">&#127754;</span>
                <h1>Ocean View Resort</h1>
                <p>Room Reservation Management System</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert error">${error}</div>
            </c:if>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert success">${sessionScope.flashSuccess}</div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>

            <form action="${ctx}/auth" method="post" class="login-form">
                <input type="hidden" name="action" value="login">
                <div class="form-group">
                    <label for="username" class="form-label">Username</label>
                    <input type="text" id="username" name="username" class="form-control" placeholder="Enter your username" required autofocus>
                </div>
                <div class="form-group">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="Enter your password" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Sign In</button>
            </form>
            <div class="login-footer">
                <p>&copy; 2025 Ocean View Resort, Galle, Sri Lanka</p>
            </div>
        </div>
    </div>
</body>
</html>

