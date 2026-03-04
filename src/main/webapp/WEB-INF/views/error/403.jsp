<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>403 — Access Denied</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
</head>
<body class="error-page">
    <div class="error-container">
        <div class="error-code">403</div>
        <h1>Access Denied</h1>
        <p>You do not have permission to access this resource.</p>
        <a href="${ctx}/dashboard" class="btn btn-primary">Go to Dashboard</a>
        <a href="${ctx}/auth?action=loginForm" class="btn btn-outline">Login</a>
    </div>
</body>
</html>

