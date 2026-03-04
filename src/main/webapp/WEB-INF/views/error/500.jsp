<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Server Error</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
</head>
<body class="error-page">
    <div class="error-container">
        <div class="error-code">500</div>
        <h1>Internal Server Error</h1>
        <p>Something went wrong on our end. Please try again later.</p>
        <a href="${ctx}/dashboard" class="btn btn-primary">Go to Dashboard</a>
    </div>
</body>
</html>

