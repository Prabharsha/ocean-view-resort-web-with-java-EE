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
    <style>
        /* ── Chart Cards ── */
        .charts-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            margin-bottom: 24px;
        }
        @media (max-width: 900px) { .charts-row { grid-template-columns: 1fr; } }

        .chart-card {
            background: var(--surface);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 24px;
            position: relative;
            overflow: hidden;
        }
        .chart-card-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            margin-bottom: 20px;
        }
        .chart-card-title {
            font-size: 15px;
            font-weight: 700;
            color: var(--primary);
        }
        .chart-card-sub {
            font-size: 12px;
            color: var(--text-muted);
            margin-top: 2px;
        }
        .chart-badge {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 4px 12px;
            border-radius: 999px;
            font-size: 11px;
            font-weight: 600;
        }
        .chart-badge--green  { background: #dcfce7; color: #16a34a; }
        .chart-badge--blue   { background: #dbeafe; color: #1d4ed8; }

        /* occupancy legend items — injected by JS */
        .occ-chart-wrap {
            display: flex;
            align-items: center;
            gap: 24px;
        }
        .occ-doughnut-wrap {
            position: relative;
            width: 200px;
            height: 200px;
            flex-shrink: 0;
        }
        .occ-center-label {
            position: absolute;
            top: 50%; left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            pointer-events: none;
        }
        .occ-center-pct   { font-size: 26px; font-weight: 800; color: var(--primary); line-height: 1; }
        .occ-center-text  { font-size: 11px; color: var(--text-muted); margin-top: 2px; }

        /* per-type progress bars */
        .occ-type-list { flex: 1; display: flex; flex-direction: column; gap: 14px; }
        .occ-type-row  { display: flex; flex-direction: column; gap: 4px; }
        .occ-type-hdr  { display: flex; justify-content: space-between; font-size: 12px; }
        .occ-type-name { font-weight: 600; color: var(--text); }
        .occ-type-pct  { color: var(--text-muted); }
        .occ-bar       { height: 8px; background: #e8eef4; border-radius: 4px; overflow: hidden; }
        .occ-bar-fill  { height: 100%; border-radius: 4px; transition: width 0.8s ease; }

        /* revenue chart area */
        .rev-chart-wrap { height: 240px; position: relative; }

        /* ── Recent Reservations full-width ── */
        .recent-card {
            background: var(--surface);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 24px;
        }
        .recent-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }
        .recent-header h3 { font-size: 15px; font-weight: 700; color: var(--primary); }
        .recent-header a  { font-size: 13px; }
    </style>
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

            <!-- ═══ Stat Cards ═══ -->
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
                            <span>${revenueTrend >= 0 ? '&#9650;' : '&#9660;'} <fmt:formatNumber value="${revenueTrend}" pattern="#,##0.0" />%</span>
                            <span class="trend-label">vs last month</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ═══ Charts Row ═══ -->
            <div class="charts-row">

                <!-- ── Occupancy Donut + Progress Bars ── -->
                <div class="chart-card">
                    <div class="chart-card-header">
                        <div>
                            <div class="chart-card-title">&#127968; Room Occupancy</div>
                            <div class="chart-card-sub">Live occupancy by room type</div>
                        </div>
                        <span class="chart-badge chart-badge--blue">Today</span>
                    </div>
                    <div class="occ-chart-wrap">
                        <div class="occ-doughnut-wrap">
                            <canvas id="occupancyDonut"></canvas>
                            <div class="occ-center-label">
                                <div class="occ-center-pct" id="occPct">–</div>
                                <div class="occ-center-text">Occupied</div>
                            </div>
                        </div>
                        <div class="occ-type-list" id="occTypeList">
                            <!-- filled by JS -->
                        </div>
                    </div>
                </div>

                <!-- ── 6-Month Revenue Bar Chart ── -->
                <div class="chart-card">
                    <div class="chart-card-header">
                        <div>
                            <div class="chart-card-title">&#128200; Revenue Overview</div>
                            <div class="chart-card-sub">Monthly revenue — last 6 months</div>
                        </div>
                        <span class="chart-badge chart-badge--green">LKR</span>
                    </div>
                    <div class="rev-chart-wrap">
                        <canvas id="revenueChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- ═══ Recent Reservations (full width) ═══ -->
            <div class="recent-card">
                <div class="recent-header">
                    <h3>&#128203; Recent Reservations</h3>
                    <a href="${ctx}/reservations?action=list" class="btn btn-sm btn-outline">View All</a>
                </div>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Res. No</th>
                                <th>Guest</th>
                                <th>Room</th>
                                <th>Check-in</th>
                                <th>Check-out</th>
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
                                    <td>${r.check_out_date}</td>
                                    <td>
                                        <span class="badge
                                            badge-${r.status == 'CONFIRMED' ? 'primary' :
                                                    r.status == 'CHECKED_IN' ? 'success' :
                                                    r.status == 'CHECKED_OUT' ? 'muted' :
                                                    r.status == 'CANCELLED' ? 'danger' : 'warning'}">
                                            ${r.status}
                                        </span>
                                    </td>
                                    <td>LKR <c:choose>
                                        <c:when test="${not empty r.total_amount}"><fmt:formatNumber value="${r.total_amount}" pattern="#,##0.00" /></c:when>
                                        <c:otherwise>0.00</c:otherwise>
                                    </c:choose></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty recentReservations}">
                                <tr><td colspan="7" class="text-center text-muted">No recent reservations</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

        </main>
    </div>
</div>
<script>var contextPath = '${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script>
// ── Occupancy Donut + Progress Bars ──────────────────────────────────────
(function() {
    var data = ${not empty occupancyJson ? occupancyJson : '[]'};
    if (!data || data.length === 0) return;

    var typeColors = ['#1a3c5e','#2e86ab','#f6ae2d','#2d9b5e'];
    var totalRooms    = data.reduce(function(s,d){ return s + (d.total||0); }, 0);
    var totalOccupied = data.reduce(function(s,d){ return s + (d.occupied||0); }, 0);
    var overallPct    = totalRooms > 0 ? Math.round(totalOccupied / totalRooms * 100) : 0;

    document.getElementById('occPct').textContent = overallPct + '%';

    // Donut chart
    var canvas = document.getElementById('occupancyDonut');
    if (canvas) {
        new Chart(canvas.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: data.map(function(d){ return d.type; }),
                datasets: [{
                    data: data.map(function(d){ return d.occupied || 0; }),
                    backgroundColor: typeColors,
                    borderWidth: 3,
                    borderColor: '#fff',
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function(ctx) {
                                var d = data[ctx.dataIndex];
                                return ' ' + d.type + ': ' + d.occupied + '/' + d.total + ' rooms (' + Math.round(d.occupancyPct||0) + '%)';
                            }
                        }
                    }
                }
            }
        });
    }

    // Per-type progress bars
    var list = document.getElementById('occTypeList');
    if (list) {
        data.forEach(function(d, i) {
            var pct = d.total > 0 ? Math.round((d.occupied / d.total) * 100) : 0;
            list.innerHTML +=
                '<div class="occ-type-row">' +
                  '<div class="occ-type-hdr">' +
                    '<span class="occ-type-name">' + d.type + '</span>' +
                    '<span class="occ-type-pct">' + d.occupied + '/' + d.total + ' &nbsp;<strong>' + pct + '%</strong></span>' +
                  '</div>' +
                  '<div class="occ-bar">' +
                    '<div class="occ-bar-fill" style="width:' + pct + '%; background:' + typeColors[i % typeColors.length] + ';"></div>' +
                  '</div>' +
                '</div>';
        });
    }
})();

// ── 6-Month Revenue Bar Chart ─────────────────────────────────────────────
(function() {
    var data = ${not empty revenueJson ? revenueJson : '[]'};
    if (!data || data.length === 0) return;

    var canvas = document.getElementById('revenueChart');
    if (!canvas) return;

    var labels   = data.map(function(d){ return d.month; });
    var revenues = data.map(function(d){ return d.revenue || 0; });
    var maxRev   = Math.max.apply(null, revenues);

    // gradient fill
    var ctx = canvas.getContext('2d');
    var gradient = ctx.createLinearGradient(0, 0, 0, 220);
    gradient.addColorStop(0,   'rgba(46,134,171,0.85)');
    gradient.addColorStop(1,   'rgba(46,134,171,0.25)');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Revenue (LKR)',
                data: revenues,
                backgroundColor: revenues.map(function(v){
                    return v === maxRev ? 'rgba(246,174,45,0.9)' : gradient;
                }),
                borderColor: revenues.map(function(v){
                    return v === maxRev ? '#e6970a' : '#2e86ab';
                }),
                borderWidth: 2,
                borderRadius: 6,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return '  LKR ' + ctx.parsed.y.toLocaleString('en-LK', {minimumFractionDigits:2});
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,0.04)' },
                    ticks: {
                        font: { size: 11 },
                        callback: function(v) {
                            return v >= 1000000 ? (v/1000000).toFixed(1)+'M'
                                 : v >= 1000    ? (v/1000).toFixed(0)+'K'
                                 : v;
                        }
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: { font: { size: 11 } }
                }
            }
        }
    });
})();
</script>
</body>
</html>

