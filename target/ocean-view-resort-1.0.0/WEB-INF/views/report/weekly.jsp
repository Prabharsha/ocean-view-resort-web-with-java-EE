<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Weekly Report — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Weekly Occupancy Report" /></jsp:include>
        <main class="main-content">
            <div class="page-header">
                <h2>Weekly Occupancy Report</h2>
                <a href="${ctx}/reports?action=exportCsv&type=weekly&weekStart=${weekStart}" class="btn btn-outline">Export CSV</a>
            </div>

            <div class="card filter-card">
                <form method="get" action="${ctx}/reports" class="filter-form">
                    <input type="hidden" name="action" value="weekly">
                    <div class="form-group">
                        <label class="form-label">Week Start Date</label>
                        <input type="date" name="weekStart" class="form-control" value="${weekStart}">
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Generate</button>
                </form>
            </div>

            <div class="card">
                <h3 class="card-title">Revenue by Day</h3>
                <div class="chart-container">
                    <canvas id="weeklyChart"></canvas>
                </div>
            </div>

            <div class="card">
                <h3 class="card-title">Daily Breakdown</h3>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead><tr><th>Date</th><th>Reservations</th><th>Revenue</th><th>Room Type</th><th>Rooms Occupied</th></tr></thead>
                        <tbody>
                        <c:forEach var="r" items="${report}">
                            <tr>
                                <td>${r.day}</td>
                                <td>${r.reservations}</td>
                                <td>LKR <fmt:formatNumber value="${r.revenue}" pattern="#,##0.00" /></td>
                                <td>${r.room_type}</td>
                                <td>${r.rooms_occupied}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty report}"><tr><td colspan="5" class="text-center text-muted">No data for this week</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script src="${ctx}/public/js/charts.js"></script>
<script>
    var weeklyData = ${not empty weeklyJson ? weeklyJson : '[]'};
    if (typeof initWeeklyChart === 'function') initWeeklyChart(weeklyData);
</script>
</body>
</html>

