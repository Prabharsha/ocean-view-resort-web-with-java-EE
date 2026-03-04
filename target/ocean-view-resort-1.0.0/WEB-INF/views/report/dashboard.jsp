<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reports Dashboard — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Reports Dashboard" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header"><h2>Reports Dashboard</h2></div>

            <div class="stats-grid">
                <div class="stat-card stat-card--primary">
                    <div class="stat-icon">&#10003;</div>
                    <div class="stat-info">
                        <span class="stat-value">${todayCheckIns}</span>
                        <span class="stat-label">Today's Check-ins</span>
                    </div>
                </div>
                <div class="stat-card stat-card--accent">
                    <div class="stat-icon">&#128176;</div>
                    <div class="stat-info">
                        <span class="stat-value">LKR <fmt:formatNumber value="${thisMonthRevenue}" pattern="#,##0.00" /></span>
                        <span class="stat-label">This Month's Revenue</span>
                    </div>
                </div>
            </div>

            <div class="card">
                <h3 class="card-title">Room Occupancy</h3>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead><tr><th>Room Type</th><th>Total</th><th>Occupied</th><th>Available</th><th>Occupancy %</th></tr></thead>
                        <tbody>
                        <c:forEach var="o" items="${occupancy}">
                            <tr>
                                <td>${o.room_type}</td><td>${o.total_rooms}</td><td>${o.occupied}</td><td>${o.available}</td>
                                <td>
                                    <div class="progress-bar"><div class="progress-fill" style="width: ${o.occupancy_pct}%">${o.occupancy_pct}%</div></div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="report-links">
                <a href="${ctx}/reports?action=monthly" class="card report-link-card">
                    <h3>Monthly Revenue Report</h3>
                    <p>View detailed monthly revenue and booking statistics</p>
                </a>
                <a href="${ctx}/reports?action=weekly" class="card report-link-card">
                    <h3>Weekly Occupancy Report</h3>
                    <p>Day-by-day occupancy and revenue breakdown</p>
                </a>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

