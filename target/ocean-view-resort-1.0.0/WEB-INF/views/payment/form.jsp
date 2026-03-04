<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Payment" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <c:choose>
                <c:when test="${not empty reservation}">
                    <!-- Payment form for a specific reservation -->
                    <div class="page-header">
                        <h2>Process Payment</h2>
                        <a href="${ctx}/reservations?action=view&id=${reservation.id}" class="btn btn-outline">Back to Reservation</a>
                    </div>

                    <div class="detail-grid">
                        <div class="card">
                            <h3 class="card-title">Reservation Details</h3>
                            <div class="detail-list">
                                <div class="detail-item"><span class="detail-label">Reservation No</span><span class="detail-value">${reservation.reservationNo}</span></div>
                                <div class="detail-item"><span class="detail-label">Guest Name</span><span class="detail-value">${guest.name}</span></div>
                                <div class="detail-item"><span class="detail-label">Room</span><span class="detail-value">${room.roomNumber} (${room.roomType})</span></div>
                                <div class="detail-item"><span class="detail-label">Check-in</span><span class="detail-value">${reservation.checkInDate}</span></div>
                                <div class="detail-item"><span class="detail-label">Check-out</span><span class="detail-value">${reservation.checkOutDate}</span></div>
                                <div class="detail-item"><span class="detail-label">Amount Due</span><span class="detail-value total-highlight">LKR <fmt:formatNumber value="${reservation.totalAmount}" pattern="#,##0.00" /></span></div>
                            </div>
                        </div>

                        <div class="card">
                            <c:choose>
                                <c:when test="${alreadyPaid}">
                                    <div class="alert warning">
                                        <strong>Payment Already Processed</strong>
                                        <p>This reservation has already been paid.</p>
                                    </div>
                                    <div class="detail-list">
                                        <div class="detail-item"><span class="detail-label">Method</span><span class="detail-value">${existingPayment.paymentMethod}</span></div>
                                        <div class="detail-item"><span class="detail-label">Reference No</span><span class="detail-value">${existingPayment.referenceNo}</span></div>
                                        <div class="detail-item"><span class="detail-label">Amount</span><span class="detail-value">LKR <fmt:formatNumber value="${existingPayment.amount}" pattern="#,##0.00" /></span></div>
                                        <div class="detail-item"><span class="detail-label">Status</span><span class="badge badge-success">${existingPayment.status}</span></div>
                                        <div class="detail-item"><span class="detail-label">Date</span><span class="detail-value">${existingPayment.paymentDate}</span></div>
                                    </div>
                                    <a href="${ctx}/payments?action=receipt&id=${existingPayment.id}" class="btn btn-primary">View Receipt</a>
                                </c:when>
                                <c:otherwise>
                                    <h3 class="card-title">Payment Details</h3>
                                    <form action="${ctx}/payments" method="post" class="payment-form">
                                        <input type="hidden" name="action" value="process">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="reservationId" value="${reservation.id}">

                                        <div class="form-group">
                                            <label class="form-label">Payment Method *</label>
                                            <div class="radio-group">
                                                <label class="radio-label">
                                                    <input type="radio" name="paymentMethod" value="CASH" required>
                                                    <span class="radio-custom"></span> Cash
                                                </label>
                                                <label class="radio-label">
                                                    <input type="radio" name="paymentMethod" value="CARD">
                                                    <span class="radio-custom"></span> Card
                                                </label>
                                            </div>
                                        </div>

                                        <div class="form-group">
                                            <label for="referenceNo" class="form-label">Reference Number *</label>
                                            <input type="text" id="referenceNo" name="referenceNo" class="form-control" required>
                                            <small id="refHelp" class="form-help">Select a payment method above</small>
                                        </div>

                                        <div class="form-group">
                                            <label for="notes" class="form-label">Notes (optional)</label>
                                            <textarea id="notes" name="notes" class="form-control" rows="3"></textarea>
                                        </div>

                                        <div class="form-actions">
                                            <button type="submit" class="btn btn-success" data-confirm="Process this payment?">Process Payment</button>
                                        </div>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <!-- Payment list -->
                    <div class="page-header"><h2>All Payments</h2></div>
                    <div class="card">
                        <div class="table-wrapper">
                            <table class="data-table">
                                <thead><tr><th>ID</th><th>Reservation</th><th>Amount</th><th>Method</th><th>Reference</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead>
                                <tbody>
                                <c:forEach var="p" items="${payments}">
                                    <tr>
                                        <td title="${p.id}">${fn:length(p.id) > 8 ? fn:substring(p.id, 0, 8).concat('...') : p.id}</td>
                                        <td>${p.reservationId}</td>
                                        <td>LKR <fmt:formatNumber value="${p.amount}" pattern="#,##0.00" /></td>
                                        <td><span class="badge badge-primary">${p.paymentMethod}</span></td>
                                        <td>${p.referenceNo}</td>
                                        <td><span class="badge badge-success">${p.status}</span></td>
                                        <td>${p.paymentDate}</td>
                                        <td><a href="${ctx}/payments?action=receipt&id=${p.id}" class="btn btn-sm btn-outline">Receipt</a></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty payments}"><tr><td colspan="8" class="text-center text-muted">No payments recorded</td></tr></c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

