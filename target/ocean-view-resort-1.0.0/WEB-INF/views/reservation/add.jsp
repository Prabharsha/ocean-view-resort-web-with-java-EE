<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Reservation — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="New Reservation" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header">
                <h2>New Reservation</h2>
                <a href="${ctx}/reservations?action=list" class="btn btn-outline">Back to List</a>
            </div>

            <div class="card">
                <form action="${ctx}/reservations" method="post" class="form-grid" id="reservationForm">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="form-group">
                        <label for="guestId" class="form-label">Guest *</label>
                        <select id="guestId" name="guestId" class="form-select" required>
                            <option value="">Select Guest</option>
                            <c:forEach var="g" items="${guests}">
                                <option value="${g.id}" ${reservation.guestId == g.id ? 'selected' : ''}>${g.name} (${g.nic})</option>
                            </c:forEach>
                        </select>
                        <a href="${ctx}/guests?action=new" class="form-link">+ Add New Guest</a>
                    </div>

                    <div class="form-group">
                        <label for="roomId" class="form-label">Room *</label>
                        <select id="roomId" name="roomId" class="form-select" required>
                            <option value="">Select Room</option>
                            <c:forEach var="rm" items="${rooms}">
                                <option value="${rm.id}" ${reservation.roomId == rm.id ? 'selected' : ''}>${rm.roomNumber} — ${rm.roomType} (LKR <fmt:formatNumber value="${rm.ratePerNight}" pattern="#,##0.00" />/night, Max ${rm.capacity} guests)</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="checkIn" class="form-label">Check-in Date *</label>
                        <input type="date" id="checkIn" name="checkInDate" class="form-control" value="${reservation.checkInDate}" required>
                    </div>

                    <div class="form-group">
                        <label for="checkOut" class="form-label">Check-out Date *</label>
                        <input type="date" id="checkOut" name="checkOutDate" class="form-control" value="${reservation.checkOutDate}" required>
                    </div>

                    <div class="form-group">
                        <label for="numGuests" class="form-label">Number of Guests *</label>
                        <input type="number" id="numGuests" name="numGuests" class="form-control" value="${reservation.numGuests != 0 ? reservation.numGuests : 1}" min="1" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Estimated Total</label>
                        <div id="totalAmount" class="total-display">LKR 0.00</div>
                    </div>

                    <div class="form-group form-group--full">
                        <label for="specialRequests" class="form-label">Special Requests</label>
                        <textarea id="specialRequests" name="specialRequests" class="form-control" rows="3">${reservation.specialRequests}</textarea>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Create Reservation</button>
                        <a href="${ctx}/reservations?action=list" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script src="${ctx}/public/js/validation.js"></script>
</body>
</html>

