<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guests — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Guests" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header">
                <h2>Guests</h2>
                <a href="${ctx}/guests?action=new" class="btn btn-primary">+ Add Guest</a>
            </div>

            <div class="card filter-card">
                <form method="get" action="${ctx}/guests" class="filter-form">
                    <input type="hidden" name="action" value="list">
                    <div class="form-group">
                        <label class="form-label">Search</label>
                        <input type="text" name="search" class="form-control" value="${param.search}" placeholder="Name, email, NIC...">
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Search</button>
                </form>
            </div>

            <div class="card">
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr><th>Name</th><th>Email</th><th>Contact</th><th>NIC</th><th>Loyalty Pts</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="g" items="${guests}">
                            <tr>
                                <td>${g.name}</td>
                                <td>${g.email}</td>
                                <td>${g.contact}</td>
                                <td>${g.nic}</td>
                                <td>${g.loyaltyPts}</td>
                                <td class="actions-cell">
                                    <a href="${ctx}/guests?action=view&id=${g.id}" class="btn btn-sm btn-outline">View</a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty guests}"><tr><td colspan="6" class="text-center text-muted">No guests found</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Guest detail panel (shown when viewing) -->
            <c:if test="${not empty guest}">
                <div class="card">
                    <h3 class="card-title">Guest: ${guest.name}</h3>
                    <div class="detail-list">
                        <div class="detail-item"><span class="detail-label">Email</span><span class="detail-value">${guest.email}</span></div>
                        <div class="detail-item"><span class="detail-label">Contact</span><span class="detail-value">${guest.contact}</span></div>
                        <div class="detail-item"><span class="detail-label">Address</span><span class="detail-value">${guest.address}</span></div>
                        <div class="detail-item"><span class="detail-label">NIC</span><span class="detail-value">${guest.nic}</span></div>
                    </div>
                    <c:if test="${not empty reservations}">
                        <h4>Reservation History</h4>
                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead><tr><th>Res. No</th><th>Check-in</th><th>Check-out</th><th>Status</th><th>Amount</th></tr></thead>
                                <tbody>
                                <c:forEach var="r" items="${reservations}">
                                    <tr>
                                        <td><a href="${ctx}/reservations?action=view&id=${r.id}">${r.reservationNo}</a></td>
                                        <td>${r.checkInDate}</td><td>${r.checkOutDate}</td>
                                        <td><span class="badge badge-${r.status == 'CONFIRMED' ? 'primary' : r.status == 'CHECKED_IN' ? 'success' : r.status == 'CANCELLED' ? 'danger' : 'warning'}">${r.status}</span></td>
                                        <td>LKR <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0.00" /></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                </div>
            </c:if>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

