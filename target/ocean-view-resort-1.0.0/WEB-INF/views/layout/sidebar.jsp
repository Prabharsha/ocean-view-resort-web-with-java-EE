<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<nav class="sidebar">
    <div class="brand">
        <span class="brand-icon">&#127754;</span>
        <span class="brand-text">Ocean View Resort</span>
    </div>
    <ul class="nav-list">
        <li class="nav-item"><a href="${ctx}/dashboard" class="nav-link"><span class="nav-icon">&#128200;</span>Dashboard</a></li>
        <li class="nav-item"><a href="${ctx}/reservations" class="nav-link"><span class="nav-icon">&#128197;</span>Reservations</a></li>
        <li class="nav-item"><a href="${ctx}/guests" class="nav-link"><span class="nav-icon">&#128101;</span>Guests</a></li>
        <li class="nav-item"><a href="${ctx}/rooms" class="nav-link"><span class="nav-icon">&#127968;</span>Rooms</a></li>
        <li class="nav-item"><a href="${ctx}/payments?action=list" class="nav-link"><span class="nav-icon">&#128179;</span>Payments</a></li>
        <c:if test="${sessionScope.userRole == 'MANAGER' or sessionScope.userRole == 'ADMIN'}">
            <li class="nav-item"><a href="${ctx}/reports?action=dashboard" class="nav-link"><span class="nav-icon">&#128202;</span>Reports</a></li>
        </c:if>
        <c:if test="${sessionScope.userRole == 'ADMIN'}">
            <li class="nav-item"><a href="${ctx}/staff" class="nav-link"><span class="nav-icon">&#128101;</span>Staff Management</a></li>
            <li class="nav-item"><a href="${ctx}/maintenance" class="nav-link"><span class="nav-icon">&#9881;</span>Maintenance</a></li>
            <li class="nav-item"><a href="${ctx}/logs" class="nav-link"><span class="nav-icon">&#128221;</span>Live Logs</a></li>
        </c:if>
    </ul>
    <div class="user-info">
        <span class="user-avatar">${fn:substring(sessionScope.loggedUser.fname, 0, 1)}${fn:substring(sessionScope.loggedUser.lname, 0, 1)}</span>
        <div class="user-details">
            <span class="user-name">${sessionScope.loggedUser.fname} ${sessionScope.loggedUser.lname}</span>
            <span class="badge">${sessionScope.userRole}</span>
        </div>
        <a href="${ctx}/auth?action=logout" class="logout-link">Logout</a>
    </div>
</nav>

<!-- Shared confirm modal (replaces native browser confirm()) -->
<div id="confirmModal" class="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="confirmModalTitle">
    <div class="modal">
        <div class="modal-header">
            <span id="confirmModalTitle" class="modal-title">Confirm Action</span>
            <button class="modal-close" id="confirmModalClose" aria-label="Close">&times;</button>
        </div>
        <p id="confirmModalMessage" class="modal-message"></p>
        <div class="modal-actions">
            <button id="confirmModalOk"     class="btn btn-primary">Confirm</button>
            <button id="confirmModalCancel" class="btn btn-outline">Cancel</button>
        </div>
    </div>
</div>

