<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Room — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <style>
        .amenity-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; margin-top: 4px; }
        .amenity-check { display: none; }
        .amenity-label { display: flex; align-items: center; gap: 8px; padding: 10px 14px; border: 2px solid var(--border); border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 500; color: var(--text-muted); background: #fff; transition: all 0.18s ease; user-select: none; }
        .amenity-label .amenity-icon { font-size: 17px; }
        .amenity-check:checked + .amenity-label { border-color: #2e86ab; background: #eaf4fb; color: #1a6080; font-weight: 600; }
        .amenity-label:hover { border-color: #2e86ab; color: #2e86ab; }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Edit Room" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header"><h2>Edit Room — ${room.roomNumber}</h2><a href="${ctx}/rooms?action=list" class="btn btn-outline">Back</a></div>
            <div class="card">
                <form action="${ctx}/rooms" method="post" class="form-grid">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="id" value="${room.id}">
                    <div class="form-group">
                        <label for="roomNumber" class="form-label">Room Number *</label>
                        <input type="text" id="roomNumber" name="roomNumber" class="form-control" value="${room.roomNumber}" required>
                    </div>
                    <div class="form-group">
                        <label for="roomType" class="form-label">Room Type *</label>
                        <select id="roomType" name="roomType" class="form-select" required>
                            <option value="STANDARD"  ${room.roomType == 'STANDARD'  ? 'selected' : ''}>Standard</option>
                            <option value="DELUXE"    ${room.roomType == 'DELUXE'    ? 'selected' : ''}>Deluxe</option>
                            <option value="SUITE"     ${room.roomType == 'SUITE'     ? 'selected' : ''}>Suite</option>
                            <option value="PENTHOUSE" ${room.roomType == 'PENTHOUSE' ? 'selected' : ''}>Penthouse</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="floor" class="form-label">Floor *</label>
                        <input type="number" id="floor" name="floor" class="form-control" value="${room.floor}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label for="capacity" class="form-label">Capacity *</label>
                        <input type="number" id="capacity" name="capacity" class="form-control" value="${room.capacity}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label for="ratePerNight" class="form-label">Rate Per Night (LKR) *</label>
                        <input type="number" id="ratePerNight" name="ratePerNight" class="form-control" value="${room.ratePerNight}" step="0.01" min="0" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Availability</label>
                        <label class="toggle-label">
                            <input type="checkbox" name="isAvailable" value="true" ${room.available ? 'checked' : ''}>
                            <span>Available</span>
                        </label>
                    </div>
                    <div class="form-group form-group--full">
                        <label class="form-label">Amenities</label>
                        <div class="amenity-grid">
                            <c:set var="cur" value="${not empty room.amenities ? room.amenities : ''}" />
                            <input type="checkbox" id="am_wifi"      name="amenities" value="Wi-Fi"       class="amenity-check" ${fn:contains(cur,'Wi-Fi')       ? 'checked' : ''}>
                            <label for="am_wifi"      class="amenity-label"><span class="amenity-icon">&#128246;</span> Wi-Fi</label>

                            <input type="checkbox" id="am_tv"        name="amenities" value="Smart TV"    class="amenity-check" ${fn:contains(cur,'Smart TV')    ? 'checked' : ''}>
                            <label for="am_tv"        class="amenity-label"><span class="amenity-icon">&#128250;</span> Smart TV</label>

                            <input type="checkbox" id="am_ac"        name="amenities" value="AC"          class="amenity-check" ${fn:contains(cur,'AC')          ? 'checked' : ''}>
                            <label for="am_ac"        class="amenity-label"><span class="amenity-icon">&#10052;</span> AC</label>

                            <input type="checkbox" id="am_phone"     name="amenities" value="Phone"       class="amenity-check" ${fn:contains(cur,'Phone')       ? 'checked' : ''}>
                            <label for="am_phone"     class="amenity-label"><span class="amenity-icon">&#128222;</span> Phone</label>

                            <input type="checkbox" id="am_kettle"    name="amenities" value="Kettle"      class="amenity-check" ${fn:contains(cur,'Kettle')      ? 'checked' : ''}>
                            <label for="am_kettle"    class="amenity-label"><span class="amenity-icon">&#9749;</span> Kettle</label>

                            <input type="checkbox" id="am_minibar"   name="amenities" value="Mini Bar"    class="amenity-check" ${fn:contains(cur,'Mini Bar')    ? 'checked' : ''}>
                            <label for="am_minibar"   class="amenity-label"><span class="amenity-icon">&#127863;</span> Mini Bar</label>

                            <input type="checkbox" id="am_balcony"   name="amenities" value="Balcony"     class="amenity-check" ${fn:contains(cur,'Balcony')     ? 'checked' : ''}>
                            <label for="am_balcony"   class="amenity-label"><span class="amenity-icon">&#127774;</span> Balcony</label>

                            <input type="checkbox" id="am_bathtub"   name="amenities" value="Bathtub"     class="amenity-check" ${fn:contains(cur,'Bathtub')     ? 'checked' : ''}>
                            <label for="am_bathtub"   class="amenity-label"><span class="amenity-icon">&#128705;</span> Bathtub</label>

                            <input type="checkbox" id="am_safe"      name="amenities" value="Safe"        class="amenity-check" ${fn:contains(cur,'Safe')        ? 'checked' : ''}>
                            <label for="am_safe"      class="amenity-label"><span class="amenity-icon">&#128272;</span> Safe</label>

                            <input type="checkbox" id="am_parking"   name="amenities" value="Parking"     class="amenity-check" ${fn:contains(cur,'Parking')     ? 'checked' : ''}>
                            <label for="am_parking"   class="amenity-label"><span class="amenity-icon">&#128663;</span> Parking</label>

                            <input type="checkbox" id="am_pool"      name="amenities" value="Pool Access" class="amenity-check" ${fn:contains(cur,'Pool Access') ? 'checked' : ''}>
                            <label for="am_pool"      class="amenity-label"><span class="amenity-icon">&#127946;</span> Pool Access</label>

                            <input type="checkbox" id="am_breakfast" name="amenities" value="Breakfast"   class="amenity-check" ${fn:contains(cur,'Breakfast')   ? 'checked' : ''}>
                            <label for="am_breakfast" class="amenity-label"><span class="amenity-icon">&#127859;</span> Breakfast</label>
                        </div>
                    </div>
                    <div class="form-group form-group--full">
                        <label for="description" class="form-label">Description</label>
                        <textarea id="description" name="description" class="form-control" rows="3">${room.description}</textarea>
                    </div>
                    <div class="form-group">
                        <label for="imageUrl" class="form-label">Image URL</label>
                        <input type="text" id="imageUrl" name="imageUrl" class="form-control" value="${room.imageUrl}">
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Update Room</button>
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

