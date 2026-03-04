<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reservation ${reservation.reservationNo} — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Reservation Details" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty sessionScope.flashError}"><div class="alert error">${sessionScope.flashError}</div><c:remove var="flashError" scope="session"/></c:if>

            <div class="page-header">
                <h2>Reservation ${reservation.reservationNo}</h2>
                <div class="header-actions">
                    <a href="${ctx}/reservations?action=list" class="btn btn-outline">Back</a>
                    <c:if test="${reservation.status != 'CANCELLED' && reservation.status != 'CHECKED_OUT'}">
                        <a href="${ctx}/reservations?action=edit&id=${reservation.id}" class="btn btn-primary">Edit</a>
                    </c:if>
                    <a href="${ctx}/reservations?action=bill&id=${reservation.id}" class="btn btn-outline">View Bill</a>
                </div>
            </div>

            <div class="detail-grid">
                <div class="card">
                    <h3 class="card-title">Reservation Info</h3>
                    <div class="detail-list">
                        <div class="detail-item"><span class="detail-label">Reservation No</span><span class="detail-value">${reservation.reservationNo}</span></div>
                        <div class="detail-item"><span class="detail-label">Status</span><span class="badge badge-${reservation.status == 'CONFIRMED' ? 'primary' : reservation.status == 'CHECKED_IN' ? 'success' : reservation.status == 'CHECKED_OUT' ? 'muted' : reservation.status == 'CANCELLED' ? 'danger' : 'warning'}">${reservation.status}</span></div>
                        <div class="detail-item"><span class="detail-label">Check-in</span><span class="detail-value">${reservation.checkInDate}</span></div>
                        <div class="detail-item"><span class="detail-label">Check-out</span><span class="detail-value">${reservation.checkOutDate}</span></div>
                        <div class="detail-item"><span class="detail-label">Guests</span><span class="detail-value">${reservation.numGuests}</span></div>
                        <div class="detail-item"><span class="detail-label">Total</span><span class="detail-value total-highlight">LKR <fmt:formatNumber value="${reservation.totalAmount}" pattern="#,##0.00" /></span></div>
                        <c:if test="${not empty reservation.specialRequests}">
                            <div class="detail-item"><span class="detail-label">Special Requests</span><span class="detail-value">${reservation.specialRequests}</span></div>
                        </c:if>
                    </div>
                </div>

                <div class="card">
                    <h3 class="card-title">Guest Info</h3>
                    <div class="detail-list">
                        <div class="detail-item"><span class="detail-label">Name</span><span class="detail-value">${guest.name}</span></div>
                        <div class="detail-item"><span class="detail-label">Email</span><span class="detail-value">${guest.email}</span></div>
                        <div class="detail-item"><span class="detail-label">Contact</span><span class="detail-value">${guest.contact}</span></div>
                        <div class="detail-item"><span class="detail-label">NIC</span><span class="detail-value">${guest.nic}</span></div>
                    </div>
                </div>

                <div class="card">
                    <h3 class="card-title">Room Info</h3>
                    <div class="detail-list">
                        <div class="detail-item"><span class="detail-label">Room</span><span class="detail-value">${room.roomNumber}</span></div>
                        <div class="detail-item"><span class="detail-label">Type</span><span class="detail-value">${room.roomType}</span></div>
                        <div class="detail-item"><span class="detail-label">Floor</span><span class="detail-value">${room.floor}</span></div>
                        <div class="detail-item"><span class="detail-label">Rate/Night</span><span class="detail-value">LKR <fmt:formatNumber value="${room.ratePerNight}" pattern="#,##0.00" /></span></div>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="card action-card">
                <h3 class="card-title">Actions
                    <span class="status-flow">
                        <span class="flow-step ${reservation.status == 'PENDING'    ? 'flow-active' : (reservation.status != 'PENDING' ? 'flow-done' : '')}">PENDING</span>
                        <span class="flow-arrow">&#8594;</span>
                        <span class="flow-step ${reservation.status == 'CONFIRMED'  ? 'flow-active' : (reservation.status == 'CHECKED_IN' || reservation.status == 'CHECKED_OUT' ? 'flow-done' : '')}">CONFIRMED</span>
                        <span class="flow-arrow">&#8594;</span>
                        <span class="flow-step ${reservation.status == 'CHECKED_IN' ? 'flow-active' : (reservation.status == 'CHECKED_OUT' ? 'flow-done' : '')}">CHECKED IN</span>
                        <span class="flow-arrow">&#8594;</span>
                        <span class="flow-step ${reservation.status == 'CHECKED_OUT'? 'flow-active' : ''}">CHECKED OUT</span>
                    </span>
                </h3>
                <div class="action-buttons">

                    <%-- STEP 1: PENDING → CONFIRMED --%>
                    <c:if test="${reservation.status == 'PENDING'}">
                        <form action="${ctx}/reservations" method="post" style="display:inline">
                            <input type="hidden" name="action"    value="confirm">
                            <input type="hidden" name="id"        value="${reservation.id}">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-primary"
                                    data-confirm="Confirm reservation ${reservation.reservationNo}?">
                                &#10004; Confirm Reservation
                            </button>
                        </form>
                    </c:if>

                    <%-- STEP 2: CONFIRMED → payment (mandatory before check-in) --%>
                    <c:if test="${reservation.status == 'CONFIRMED'}">
                        <c:choose>
                            <c:when test="${not empty payment}">
                                <%-- Payment done — allow Check In --%>
                                <form action="${ctx}/reservations" method="post" style="display:inline">
                                    <input type="hidden" name="action"    value="checkin">
                                    <input type="hidden" name="id"        value="${reservation.id}">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <button type="submit" class="btn btn-success"
                                            data-confirm="Check in guest for ${reservation.reservationNo}?">
                                        &#10004; Check In
                                    </button>
                                </form>
                                <a href="${ctx}/payments?action=receipt&id=${payment.id}" class="btn btn-outline">View Receipt</a>
                            </c:when>
                            <c:otherwise>
                                <%-- Payment required first --%>
                                <a href="${ctx}/payments?action=form&reservationId=${reservation.id}"
                                   class="btn btn-success">Process Payment &amp; Check In</a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <%-- STEP 3: CHECKED_IN → CHECKED_OUT --%>
                    <c:if test="${reservation.status == 'CHECKED_IN'}">
                        <c:if test="${empty payment}">
                            <a href="${ctx}/payments?action=form&reservationId=${reservation.id}"
                               class="btn btn-warning">Process Payment First</a>
                        </c:if>
                        <c:if test="${not empty payment}">
                            <form action="${ctx}/reservations" method="post" style="display:inline">
                                <input type="hidden" name="action"    value="checkout">
                                <input type="hidden" name="id"        value="${reservation.id}">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <button type="submit" class="btn btn-primary"
                                        data-confirm="Check out guest for ${reservation.reservationNo}?">
                                    &#10004; Check Out
                                </button>
                            </form>
                            <a href="${ctx}/payments?action=receipt&id=${payment.id}" class="btn btn-outline">View Receipt</a>
                        </c:if>
                    </c:if>

                    <%-- CHECKED_OUT — receipt only --%>
                    <c:if test="${reservation.status == 'CHECKED_OUT' && not empty payment}">
                        <a href="${ctx}/payments?action=receipt&id=${payment.id}" class="btn btn-outline">View Receipt</a>
                    </c:if>

                    <%-- Cancel only allowed for PENDING and CONFIRMED --%>
                    <c:if test="${reservation.status == 'PENDING' || reservation.status == 'CONFIRMED'}">
                        <form action="${ctx}/reservations" method="post" style="display:inline">
                            <input type="hidden" name="action"    value="cancel">
                            <input type="hidden" name="id"        value="${reservation.id}">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-danger"
                                    data-confirm="Cancel reservation ${reservation.reservationNo}? This cannot be undone.">
                                &#10006; Cancel Reservation
                            </button>
                        </form>
                    </c:if>

                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

