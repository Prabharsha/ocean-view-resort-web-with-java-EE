<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bill — ${reservation.reservationNo}</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/print.css" media="print">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Invoice" /></jsp:include>
        <main class="main-content">
            <div class="page-header no-print">
                <h2>Invoice / Bill</h2>
                <div class="header-actions">
                    <button onclick="window.print()" class="btn btn-primary">Print Bill</button>
                    <c:if test="${not empty payment}">
                        <a href="${ctx}/payments?action=receipt&id=${payment.id}" class="btn btn-outline">View Receipt</a>
                    </c:if>
                    <a href="${ctx}/reservations?action=view&id=${reservation.id}" class="btn btn-outline">Back</a>
                </div>
            </div>
            <div class="card bill-card">
                <div class="bill-header">
                    <div class="bill-logo">
                        <h1>&#127754; Ocean View Resort</h1>
                        <p>Galle, Sri Lanka</p>
                        <p>Tel: +94 91 234 5678 | info@oceanviewresort.lk</p>
                    </div>
                    <div class="bill-meta">
                        <h2>INVOICE</h2>
                        <p><strong>Reservation:</strong> ${reservation.reservationNo}</p>
                        <p><strong>Date:</strong> ${reservation.createdAt}</p>
                        <p><strong>Status:</strong>
                            <span class="badge badge-${reservation.status == 'CONFIRMED' ? 'primary' : reservation.status == 'CHECKED_IN' ? 'success' : reservation.status == 'CHECKED_OUT' ? 'muted' : reservation.status == 'CANCELLED' ? 'danger' : 'warning'}">${reservation.status}</span>
                        </p>
                    </div>
                </div>
                <hr>
                <div class="bill-details">
                    <div class="bill-section">
                        <h3>Guest Details</h3>
                        <p><strong>Name:</strong> ${guest.name}</p>
                        <p><strong>Email:</strong> ${guest.email}</p>
                        <p><strong>Contact:</strong> ${guest.contact}</p>
                        <p><strong>NIC:</strong> ${guest.nic}</p>
                    </div>
                    <div class="bill-section">
                        <h3>Stay Details</h3>
                        <p><strong>Room:</strong> ${room.roomNumber} (${room.roomType})</p>
                        <p><strong>Check-in:</strong> ${reservation.checkInDate}</p>
                        <p><strong>Check-out:</strong> ${reservation.checkOutDate}</p>
                        <p><strong>Guests:</strong> ${reservation.numGuests}</p>
                    </div>
                </div>
                <table class="bill-table">
                    <thead>
                        <tr><th>Description</th><th>Rate/Night</th><th>Nights</th><th>Amount</th></tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>${room.roomType} Room — ${room.roomNumber}</td>
                            <td>LKR <fmt:formatNumber value="${room.ratePerNight}" pattern="#,##0.00" /></td>
                            <td>${reservation.nights}</td>
                            <td>LKR <fmt:formatNumber value="${reservation.totalAmount}" pattern="#,##0.00" /></td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="bill-total">
                            <td colspan="3"><strong>Total Amount</strong></td>
                            <td><strong>LKR <fmt:formatNumber value="${reservation.totalAmount}" pattern="#,##0.00" /></strong></td>
                        </tr>
                    </tfoot>
                </table>

                <%-- Payment section — shown only when payment has been processed --%>
                <c:choose>
                    <c:when test="${not empty payment}">
                        <div style="margin-top:24px; padding:20px; background:#f0fdf4; border-radius:8px; border-left:4px solid #2d9b5e;">
                            <h3 style="font-size:15px; font-weight:700; color:#065f46; margin-bottom:14px;">&#10003; Payment Received</h3>
                            <table class="bill-table" style="margin:0;">
                                <tbody>
                                    <tr>
                                        <td style="color:#4b5563; font-weight:500;">Payment Date</td>
                                        <td>${payment.paymentDate}</td>
                                        <td style="color:#4b5563; font-weight:500;">Method</td>
                                        <td><span class="badge badge-primary">${payment.paymentMethod}</span></td>
                                    </tr>
                                    <tr>
                                        <td style="color:#4b5563; font-weight:500;">Reference No</td>
                                        <td>${payment.referenceNo}</td>
                                        <td style="color:#4b5563; font-weight:500;">Status</td>
                                        <td><span class="badge badge-success">${payment.status}</span></td>
                                    </tr>
                                    <c:if test="${not empty payment.notes}">
                                        <tr>
                                            <td style="color:#4b5563; font-weight:500;">Notes</td>
                                            <td colspan="3">${payment.notes}</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                                <tfoot>
                                    <tr class="bill-total">
                                        <td colspan="3"><strong>Amount Paid</strong></td>
                                        <td><strong style="color:#2d9b5e;">LKR <fmt:formatNumber value="${payment.amount}" pattern="#,##0.00" /></strong></td>
                                    </tr>
                                    <tr class="bill-total">
                                        <td colspan="3"><strong>Balance Due</strong></td>
                                        <td><strong style="color:#1a3c5e;">LKR 0.00</strong></td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="margin-top:24px; padding:16px 20px; background:#fff4e5; border-radius:8px; border-left:4px solid #e67e22; font-size:14px; color:#92400e;">
                            &#9888; <strong>Payment Pending</strong> — No payment has been recorded for this reservation yet.
                            <c:if test="${reservation.status != 'CANCELLED' && reservation.status != 'CHECKED_OUT'}">
                                <a href="${ctx}/payments?action=form&reservationId=${reservation.id}" class="btn btn-sm btn-success" style="margin-left:12px;">Process Payment</a>
                            </c:if>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty reservation.specialRequests}">
                    <div class="bill-notes" style="margin-top:20px;">
                        <h4>Special Requests:</h4>
                        <p>${reservation.specialRequests}</p>
                    </div>
                </c:if>
                <div class="bill-footer">
                    <p>Thank you for choosing Ocean View Resort!</p>
                    <p class="small">This is a computer-generated invoice.</p>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

