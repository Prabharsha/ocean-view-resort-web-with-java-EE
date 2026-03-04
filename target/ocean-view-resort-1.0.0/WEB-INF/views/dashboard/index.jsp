<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <link rel="stylesheet" href="${ctx}/public/css/print.css" media="print">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp">
            <jsp:param name="pageTitle" value="Dashboard" />
        </jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert success">${sessionScope.flashSuccess}</div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert error">${error}</div>
            </c:if>

            <!-- Stat Cards -->
            <div class="stats-grid">
                <div class="stat-card stat-card--primary">
                    <div class="stat-icon">&#128197;</div>
                    <div class="stat-info">
                        <span class="stat-value">${todayReservations}</span>
                        <span class="stat-label">Today's Reservations</span>
                    </div>
                </div>
                <div class="stat-card stat-card--success">
                    <div class="stat-icon">&#10003;</div>
                    <div class="stat-info">
                        <span class="stat-value">${todayCheckIns}</span>
                        <span class="stat-label">Check-ins Today</span>
                    </div>
                </div>
                <div class="stat-card stat-card--warning">
                    <div class="stat-icon">&#128682;</div>
                    <div class="stat-info">
                        <span class="stat-value">${todayCheckOuts}</span>
                        <span class="stat-label">Check-outs Today</span>
                    </div>
                </div>
                <div class="stat-card stat-card--info">
                    <div class="stat-icon">&#127968;</div>
                    <div class="stat-info">
                        <span class="stat-value">${availableRooms}</span>
                        <span class="stat-label">Available Rooms</span>
                    </div>
                </div>
                <div class="stat-card stat-card--accent">
                    <div class="stat-icon">&#128176;</div>
                    <div class="stat-info">
                        <span class="stat-value">LKR <fmt:formatNumber value="${not empty thisMonthRevenue ? thisMonthRevenue : 0}" pattern="#,##0.00" /></span>
                        <span class="stat-label">Revenue This Month</span>
                        <div class="stat-trend ${revenueTrend >= 0 ? 'trend-up' : 'trend-down'}">
                            <span><fmt:formatNumber value="${revenueTrend}" pattern="#,##0.0" />%</span>
                            <span class="trend-arrow">${revenueTrend >= 0 ? '&#9650;' : '&#9660;'}</span>
                            <span class="trend-label">vs last month</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="dashboard-grid">
                <!-- Occupancy Chart -->
                <div class="card">
                    <h3 class="card-title">Room Occupancy</h3>
                    <div class="chart-container">
                        <canvas id="occupancyChart"></canvas>
                    </div>
                </div>

                <!-- Recent Reservations -->
                <div class="card">
                    <h3 class="card-title">Recent Reservations</h3>
                    <div class="table-wrapper">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Res. No</th>
                                    <th>Guest</th>
                                    <th>Room</th>
                                    <th>Check-in</th>
                                    <th>Status</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="r" items="${recentReservations}">
                                    <tr>
                                        <td><a href="${ctx}/reservations?action=view&id=${r.id}">${r.reservation_no}</a></td>
                                        <td>${r.guest_name}</td>
                                        <td>${r.room_number} (${r.room_type})</td>
                                        <td>${r.check_in_date}</td>
                                        <td><span class="badge badge-${r.status == 'CONFIRMED' ? 'primary' : r.status == 'CHECKED_IN' ? 'success' : r.status == 'CHECKED_OUT' ? 'muted' : r.status == 'CANCELLED' ? 'danger' : 'warning'}">${r.status}</span></td>
                                        <td>LKR <c:choose><c:when test="${not empty r.total_amount}"><fmt:formatNumber value="${r.total_amount}" pattern="#,##0.00" /></c:when><c:otherwise>0.00</c:otherwise></c:choose></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty recentReservations}">
                                    <tr><td colspan="6" class="text-center text-muted">No recent reservations</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath = '${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script src="${ctx}/public/js/charts.js"></script>
<script>
    var occupancyData = ${not empty occupancyJson ? occupancyJson : '[]'};
    if (typeof initOccupancyChart === 'function') initOccupancyChart(occupancyData);
</script>
</body>
</html>

