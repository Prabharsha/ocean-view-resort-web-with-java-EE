<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Reservation — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Edit Reservation" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header">
                <h2>Edit Reservation — ${reservation.reservationNo}</h2>
                <a href="${ctx}/reservations?action=view&id=${reservation.id}" class="btn btn-outline">Back</a>
            </div>
            <div class="card">
                <form action="${ctx}/reservations" method="post" class="form-grid">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="id" value="${reservation.id}">
                    <input type="hidden" name="status" value="${reservation.status}">

                    <div class="form-group">
                        <label for="guestId" class="form-label">Guest *</label>
                        <select id="guestId" name="guestId" class="form-select" required>
                            <c:forEach var="g" items="${guests}">
                                <option value="${g.id}" ${reservation.guestId == g.id ? 'selected' : ''}>${g.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="roomId" class="form-label">Room *</label>
                        <select id="roomId" name="roomId" class="form-select" required>
                            <c:forEach var="rm" items="${rooms}">
                                <option value="${rm.id}" ${reservation.roomId == rm.id ? 'selected' : ''}>${rm.roomNumber} — ${rm.roomType}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="checkIn" class="form-label">Check-in *</label>
                        <input type="date" id="checkIn" name="checkInDate" class="form-control" value="${reservation.checkInDate}" required>
                    </div>
                    <div class="form-group">
                        <label for="checkOut" class="form-label">Check-out *</label>
                        <input type="date" id="checkOut" name="checkOutDate" class="form-control" value="${reservation.checkOutDate}" required>
                    </div>
                    <div class="form-group">
                        <label for="numGuests" class="form-label">Guests *</label>
                        <input type="number" id="numGuests" name="numGuests" class="form-control" value="${reservation.numGuests}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Estimated Total</label>
                        <div id="totalAmount" class="total-display">LKR <fmt:formatNumber value="${reservation.totalAmount}" pattern="#,##0.00" /></div>
                    </div>
                    <div class="form-group form-group--full">
                        <label for="specialRequests" class="form-label">Special Requests</label>
                        <textarea id="specialRequests" name="specialRequests" class="form-control" rows="3">${reservation.specialRequests}</textarea>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Update Reservation</button>
                        <a href="${ctx}/reservations?action=view&id=${reservation.id}" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

