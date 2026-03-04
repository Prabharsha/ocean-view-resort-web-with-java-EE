<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${param.title != null ? param.title : 'Ocean View Resort'}</title>
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <link rel="stylesheet" href="${ctx}/public/css/print.css" media="print">
</head>
<body>
    <div class="app-layout">
        <jsp:include page="sidebar.jsp" />
        <div class="main-wrapper">
            <jsp:include page="navbar.jsp" />
            <main class="main-content">
                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert success">${sessionScope.flashSuccess}</div>
                    <c:remove var="flashSuccess" scope="session"/>
                </c:if>
                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert error">${sessionScope.flashError}</div>
                    <c:remove var="flashError" scope="session"/>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert error">${error}</div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert success">${success}</div>
                </c:if>
                <jsp:doBody />
            </main>
        </div>
    </div>
    <script>var contextPath = '${ctx}';</script>
    <script src="${ctx}/public/js/main.js"></script>
    <script src="${ctx}/public/js/validation.js"></script>
</body>
</html>

