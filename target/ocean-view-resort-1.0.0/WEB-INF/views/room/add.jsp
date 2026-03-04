<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Room — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Add Room" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header"><h2>Add New Room</h2><a href="${ctx}/rooms?action=list" class="btn btn-outline">Back</a></div>
            <div class="card">
                <form action="${ctx}/rooms" method="post" class="form-grid">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="form-group">
                        <label for="roomNumber" class="form-label">Room Number *</label>
                        <input type="text" id="roomNumber" name="roomNumber" class="form-control" value="${room.roomNumber}" required>
                    </div>
                    <div class="form-group">
                        <label for="roomType" class="form-label">Room Type *</label>
                        <select id="roomType" name="roomType" class="form-select" required>
                            <option value="">Select Type</option>
                            <option value="STANDARD" ${room.roomType == 'STANDARD' ? 'selected' : ''}>Standard</option>
                            <option value="DELUXE" ${room.roomType == 'DELUXE' ? 'selected' : ''}>Deluxe</option>
                            <option value="SUITE" ${room.roomType == 'SUITE' ? 'selected' : ''}>Suite</option>
                            <option value="PENTHOUSE" ${room.roomType == 'PENTHOUSE' ? 'selected' : ''}>Penthouse</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="floor" class="form-label">Floor *</label>
                        <input type="number" id="floor" name="floor" class="form-control" value="${room.floor}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label for="capacity" class="form-label">Capacity *</label>
                        <input type="number" id="capacity" name="capacity" class="form-control" value="${room.capacity != 0 ? room.capacity : 2}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label for="ratePerNight" class="form-label">Rate Per Night (LKR) *</label>
                        <input type="number" id="ratePerNight" name="ratePerNight" class="form-control" value="${room.ratePerNight}" step="0.01" min="0" required>
                    </div>
                    <div class="form-group form-group--full">
                        <label for="description" class="form-label">Description</label>
                        <textarea id="description" name="description" class="form-control" rows="3">${room.description}</textarea>
                    </div>
                    <div class="form-group form-group--full">
                        <label for="amenities" class="form-label">Amenities (JSON)</label>
                        <textarea id="amenities" name="amenities" class="form-control" rows="2">${room.amenities}</textarea>
                    </div>
                    <div class="form-group">
                        <label for="imageUrl" class="form-label">Image URL</label>
                        <input type="text" id="imageUrl" name="imageUrl" class="form-control" value="${room.imageUrl}">
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Add Room</button>
                        <a href="${ctx}/rooms?action=list" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

