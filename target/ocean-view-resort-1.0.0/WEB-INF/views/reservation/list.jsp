<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reservations — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Reservations" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty sessionScope.flashError}"><div class="alert error">${sessionScope.flashError}</div><c:remove var="flashError" scope="session"/></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header">
                <h2>Reservations</h2>
                <a href="${ctx}/reservations?action=new" class="btn btn-primary">+ New Reservation</a>
            </div>

            <!-- Filter -->
            <div class="card filter-card">
                <form method="get" action="${ctx}/reservations" class="filter-form">
                    <input type="hidden" name="action" value="list">
                    <div class="form-group">
                        <label class="form-label">Status</label>
                        <select name="status" class="form-select">
                            <option value="">All</option>
                            <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                            <option value="CONFIRMED" ${param.status == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                            <option value="CHECKED_IN" ${param.status == 'CHECKED_IN' ? 'selected' : ''}>Checked In</option>
                            <option value="CHECKED_OUT" ${param.status == 'CHECKED_OUT' ? 'selected' : ''}>Checked Out</option>
                            <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Filter</button>
                </form>
            </div>

            <div class="card">
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Res. No</th><th>Guest</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Status</th><th>Amount</th><th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="r" items="${reservations}">
                            <tr>
                                <td><a href="${ctx}/reservations?action=view&id=${r.id}">${r.reservationNo}</a></td>
                                <td>${not empty r.guestName ? r.guestName : r.guestId}</td>
                                <td>${not empty r.roomNumber ? r.roomNumber : r.roomId}</td>
                                <td>${r.checkInDate}</td>
                                <td>${r.checkOutDate}</td>
                                <td><span class="badge badge-${r.status == 'CONFIRMED' ? 'primary' : r.status == 'CHECKED_IN' ? 'success' : r.status == 'CHECKED_OUT' ? 'muted' : r.status == 'CANCELLED' ? 'danger' : 'warning'}">${r.status}</span></td>
                                <td>LKR <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0.00" /></td>
                                <td class="actions-cell">
                                    <a href="${ctx}/reservations?action=view&id=${r.id}" class="btn btn-sm btn-action-view">View</a>
                                    <c:if test="${r.status != 'CANCELLED' && r.status != 'CHECKED_OUT'}">
                                        <a href="${ctx}/reservations?action=edit&id=${r.id}" class="btn btn-sm btn-action-edit">Edit</a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty reservations}">
                            <tr><td colspan="8" class="text-center text-muted">No reservations found</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

