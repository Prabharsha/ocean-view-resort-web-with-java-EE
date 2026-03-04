<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rooms — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Rooms" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty sessionScope.flashError}"><div class="alert error">${sessionScope.flashError}</div><c:remove var="flashError" scope="session"/></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header">
                <h2>${maintenanceView ? 'Room Maintenance' : 'Rooms'}</h2>
                <c:if test="${not maintenanceView}">
                    <a href="${ctx}/rooms?action=new" class="btn btn-primary">+ Add Room</a>
                </c:if>
            </div>

            <div class="card filter-card">
                <form method="get" action="${ctx}/${maintenanceView ? 'maintenance' : 'rooms'}" class="filter-form">
                    <c:if test="${maintenanceView}"><input type="hidden" name="action" value="rooms"></c:if>
                    <c:if test="${not maintenanceView}"><input type="hidden" name="action" value="list"></c:if>
                    <div class="form-group">
                        <label class="form-label">Type</label>
                        <select name="roomType" class="form-select">
                            <option value="">All Types</option>
                            <option value="STANDARD" ${param.roomType == 'STANDARD' ? 'selected' : ''}>Standard</option>
                            <option value="DELUXE" ${param.roomType == 'DELUXE' ? 'selected' : ''}>Deluxe</option>
                            <option value="SUITE" ${param.roomType == 'SUITE' ? 'selected' : ''}>Suite</option>
                            <option value="PENTHOUSE" ${param.roomType == 'PENTHOUSE' ? 'selected' : ''}>Penthouse</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Floor</label>
                        <input type="number" name="floor" class="form-control" value="${param.floor}" placeholder="Any">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Check-in</label>
                        <input type="date" name="checkIn" class="form-control" value="${param.checkIn}">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Check-out</label>
                        <input type="date" name="checkOut" class="form-control" value="${param.checkOut}">
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Filter</button>
                </form>
            </div>

            <div class="card">
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr><th>Room No</th><th>Type</th><th>Floor</th><th>Capacity</th><th>Rate/Night</th><th>Status</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="rm" items="${rooms}">
                            <tr>
                                <td>${rm.roomNumber}</td>
                                <td><span class="badge badge-primary">${rm.roomType}</span></td>
                                <td>${rm.floor}</td>
                                <td>${rm.capacity}</td>
                                <td>LKR <fmt:formatNumber value="${rm.ratePerNight}" pattern="#,##0.00" /></td>
                                <td><span class="badge ${rm.available ? 'badge-success' : 'badge-danger'}">${rm.available ? 'Available' : 'Unavailable'}</span></td>
                                <td class="actions-cell">
                                    <c:if test="${maintenanceView}">
                                        <form action="${ctx}/maintenance" method="post" style="display:inline">
                                            <input type="hidden" name="action" value="toggleRoom">
                                            <input type="hidden" name="roomId" value="${rm.id}">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <button type="submit" class="btn btn-sm ${rm.available ? 'btn-danger' : 'btn-success'}">${rm.available ? 'Set Unavailable' : 'Set Available'}</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${not maintenanceView}">
                                        <a href="${ctx}/rooms?action=edit&id=${rm.id}" class="btn btn-sm btn-action-edit">Edit</a>
                                        <form action="${ctx}/rooms" method="post" style="display:inline">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${rm.id}">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <button type="submit" class="btn btn-sm btn-action-delete" data-confirm="Delete this room?">Delete</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty rooms}"><tr><td colspan="7" class="text-center text-muted">No rooms found</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

