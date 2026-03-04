<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Monthly Report — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Monthly Revenue Report" /></jsp:include>
        <main class="main-content">
            <div class="page-header">
                <h2>Monthly Revenue Report</h2>
                <a href="${ctx}/reports?action=exportCsv&type=monthly&year=${year}&month=${month}" class="btn btn-outline">Export CSV</a>
            </div>

            <div class="card filter-card">
                <form method="get" action="${ctx}/reports" class="filter-form">
                    <input type="hidden" name="action" value="monthly">
                    <div class="form-group">
                        <label class="form-label">Year</label>
                        <input type="number" name="year" class="form-control" value="${year}" min="2020" max="2030">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Month</label>
                        <select name="month" class="form-select">
                            <c:forEach var="m" begin="1" end="12">
                                <option value="${m}" ${month == m ? 'selected' : ''}>${m}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Generate</button>
                </form>
            </div>

            <c:if test="${not empty report}">
                <div class="stats-grid">
                    <c:forEach var="r" items="${report}">
                        <div class="stat-card stat-card--primary">
                            <div class="stat-info"><span class="stat-value">${r.total_reservations}</span><span class="stat-label">Total Reservations</span></div>
                        </div>
                        <div class="stat-card stat-card--accent">
                            <div class="stat-info"><span class="stat-value">LKR <fmt:formatNumber value="${r.total_revenue}" pattern="#,##0.00" /></span><span class="stat-label">Total Revenue</span></div>
                        </div>
                        <div class="stat-card stat-card--info">
                            <div class="stat-info"><span class="stat-value">LKR <fmt:formatNumber value="${r.avg_revenue}" pattern="#,##0.00" /></span><span class="stat-label">Avg Per Booking</span></div>
                        </div>
                        <div class="stat-card stat-card--danger">
                            <div class="stat-info"><span class="stat-value">${r.cancellations}</span><span class="stat-label">Cancellations</span></div>
                        </div>
                        <div class="stat-card stat-card--success">
                            <div class="stat-info"><span class="stat-value">${r.card_payments}</span><span class="stat-label">Card Payments</span></div>
                        </div>
                        <div class="stat-card stat-card--warning">
                            <div class="stat-info"><span class="stat-value">${r.cash_payments}</span><span class="stat-label">Cash Payments</span></div>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <c:if test="${not empty details}">
                <div class="card">
                    <h3 class="card-title">Reservation Details</h3>
                    <div class="table-wrapper">
                        <table class="data-table">
                            <thead><tr><th>Res. No</th><th>Guest</th><th>Room</th><th>Check-in</th><th>Check-out</th><th>Nights</th><th>Amount</th><th>Payment</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach var="d" items="${details}">
                                <tr>
                                    <td>${d.reservation_no}</td><td>${d.guest_name}</td><td>${d.room_number} (${d.room_type})</td>
                                    <td>${d.check_in_date}</td><td>${d.check_out_date}</td><td>${d.nights}</td>
                                    <td>LKR <fmt:formatNumber value="${d.total_amount}" pattern="#,##0.00" /></td>
                                    <td>${d.payment_method != null ? d.payment_method : '-'}</td>
                                    <td><span class="badge">${d.status}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

