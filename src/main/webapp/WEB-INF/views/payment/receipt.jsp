 <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Receipt — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/print.css" media="print">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Payment Receipt" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success no-print">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>

            <div class="page-header no-print">
                <h2>Payment Receipt</h2>
                <div class="header-actions">
                    <button onclick="window.print()" class="btn btn-primary">Print Receipt</button>
                    <a href="${ctx}/reservations?action=view&id=${reservation.id}" class="btn btn-outline">Back to Reservation</a>
                </div>
            </div>

            <div class="card receipt-card">
                <div class="receipt-header">
                    <div class="receipt-logo">
                        <h1>&#127754; Ocean View Resort</h1>
                        <p>Galle, Sri Lanka</p>
                        <p>Tel: +94 91 234 5678</p>
                    </div>
                    <div class="receipt-meta">
                        <h2>PAYMENT RECEIPT</h2>
                        <p><strong>Receipt ID:</strong> ${fn:length(payment.id) > 8 ? fn:substring(payment.id, 0, 8) : payment.id}</p>
                        <p><strong>Date:</strong> ${payment.paymentDate}</p>
                    </div>
                </div>
                <hr>
                <div class="receipt-body">
                    <div class="receipt-section">
                        <h3>Guest Details</h3>
                        <p><strong>Name:</strong> ${guest.name}</p>
                        <p><strong>Email:</strong> ${guest.email}</p>
                        <p><strong>Contact:</strong> ${guest.contact}</p>
                    </div>
                    <div class="receipt-section">
                        <h3>Reservation Details</h3>
                        <p><strong>Reservation No:</strong> ${reservation.reservationNo}</p>
                        <p><strong>Room:</strong> ${room.roomNumber} (${room.roomType})</p>
                        <p><strong>Check-in:</strong> ${reservation.checkInDate}</p>
                        <p><strong>Check-out:</strong> ${reservation.checkOutDate}</p>
                    </div>
                </div>
                <table class="receipt-table">
                    <thead><tr><th>Description</th><th>Details</th></tr></thead>
                    <tbody>
                        <tr><td>Payment Method</td><td><span class="badge badge-primary">${payment.paymentMethod}</span></td></tr>
                        <tr><td>Reference Number</td><td>${payment.referenceNo}</td></tr>
                        <tr><td>Status</td><td><span class="badge badge-success">${payment.status}</span></td></tr>
                        <c:if test="${not empty payment.notes}"><tr><td>Notes</td><td>${payment.notes}</td></tr></c:if>
                    </tbody>
                    <tfoot>
                        <tr class="receipt-total">
                            <td><strong>Total Amount Paid</strong></td>
                            <td><strong>LKR <fmt:formatNumber value="${payment.amount}" pattern="#,##0.00" /></strong></td>
                        </tr>
                    </tfoot>
                </table>
                <div class="receipt-footer">
                    <p>Thank you for your payment!</p>
                    <p class="small">This is a computer-generated receipt. No signature required.</p>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

