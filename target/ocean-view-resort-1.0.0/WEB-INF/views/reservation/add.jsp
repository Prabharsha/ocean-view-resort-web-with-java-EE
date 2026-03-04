<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Reservation — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <style>
        .amenity-chips { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
        .amenity-chip {
            display: inline-flex; align-items: center; gap: 4px;
            padding: 4px 12px; border-radius: 999px;
            font-size: 12px; font-weight: 600;
            background: #eaf4fb; color: #1a6080;
            border: 1px solid #b3d9ee;
        }
        .room-amenities-box {
            display: none;
            margin-top: 10px;
            padding: 12px 14px;
            background: #f8fbfe;
            border: 1px solid #d0eaf7;
            border-radius: 8px;
        }
        .room-amenities-box .am-label {
            font-size: 12px; font-weight: 600;
            color: var(--text-muted); margin-bottom: 6px;
            text-transform: uppercase; letter-spacing: 0.5px;
        }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="New Reservation" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>

            <div class="page-header">
                <h2>New Reservation</h2>
                <a href="${ctx}/reservations?action=list" class="btn btn-outline">Back to List</a>
            </div>

            <div class="card">
                <form action="${ctx}/reservations" method="post" class="form-grid" id="reservationForm">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">

                    <div class="form-group">
                        <label for="guestId" class="form-label">Guest *</label>
                        <select id="guestId" name="guestId" class="form-select" required>
                            <option value="">Select Guest</option>
                            <c:forEach var="g" items="${guests}">
                                <option value="${g.id}" ${reservation.guestId == g.id ? 'selected' : ''}>${g.name} (${g.nic})</option>
                            </c:forEach>
                        </select>
                        <a href="${ctx}/guests?action=new" class="form-link">+ Add New Guest</a>
                    </div>

                    <div class="form-group">
                        <label for="roomId" class="form-label">Room *</label>
                        <select id="roomId" name="roomId" class="form-select" required>
                            <option value="">Select Room</option>
                            <c:forEach var="rm" items="${rooms}">
                                <option value="${rm.id}"
                                        data-amenities="${rm.amenities}"
                                        ${reservation.roomId == rm.id ? 'selected' : ''}>
                                    ${rm.roomNumber} — ${rm.roomType} (LKR <fmt:formatNumber value="${rm.ratePerNight}" pattern="#,##0.00" />/night, Max ${rm.capacity} guests)
                                </option>
                            </c:forEach>
                        </select>
                        <%-- Amenity chips panel — shown on room selection --%>
                        <div class="room-amenities-box" id="amenitiesBox">
                            <div class="am-label">&#127968; Room Amenities</div>
                            <div class="amenity-chips" id="amenityChips"></div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="checkIn" class="form-label">Check-in Date *</label>
                        <input type="date" id="checkIn" name="checkInDate" class="form-control" value="${reservation.checkInDate}" required>
                    </div>

                    <div class="form-group">
                        <label for="checkOut" class="form-label">Check-out Date *</label>
                        <input type="date" id="checkOut" name="checkOutDate" class="form-control" value="${reservation.checkOutDate}" required>
                    </div>

                    <div class="form-group">
                        <label for="numGuests" class="form-label">Number of Guests *</label>
                        <input type="number" id="numGuests" name="numGuests" class="form-control" value="${reservation.numGuests != 0 ? reservation.numGuests : 1}" min="1" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Estimated Total</label>
                        <div id="totalAmount" class="total-display">LKR 0.00</div>
                    </div>

                    <div class="form-group form-group--full">
                        <label for="specialRequests" class="form-label">Special Requests</label>
                        <textarea id="specialRequests" name="specialRequests" class="form-control" rows="3">${reservation.specialRequests}</textarea>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Create Reservation</button>
                        <a href="${ctx}/reservations?action=list" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script src="${ctx}/public/js/validation.js"></script>
<script>
// Amenity icon map
var amenityIcons = {
    'Wi-Fi':       '&#128246;',
    'Smart TV':    '&#128250;',
    'AC':          '&#10052;',
    'Phone':       '&#128222;',
    'Kettle':      '&#9749;',
    'Mini Bar':    '&#127863;',
    'Balcony':     '&#127774;',
    'Bathtub':     '&#128705;',
    'Safe':        '&#128272;',
    'Parking':     '&#128663;',
    'Pool Access': '&#127946;',
    'Breakfast':   '&#127859;'
};

function showAmenities(amenitiesStr) {
    var box   = document.getElementById('amenitiesBox');
    var chips = document.getElementById('amenityChips');
    chips.innerHTML = '';
    if (!amenitiesStr || amenitiesStr.trim() === '') { box.style.display = 'none'; return; }
    var list;
    // Handle JSON array format: ["WiFi","AC","TV"] or comma-separated: Wi-Fi,AC
    var trimmed = amenitiesStr.trim();
    if (trimmed.charAt(0) === '[') {
        // JSON array
        try {
            list = JSON.parse(trimmed);
        } catch(e) {
            // Fallback: strip brackets and split
            list = trimmed.replace(/^\[|\]$/g,'').split(',').map(function(s){
                return s.trim().replace(/^["']|["']$/g,'');
            });
        }
    } else {
        list = trimmed.split(',').map(function(s){ return s.trim(); });
    }
    list = list.filter(function(a){ return a.length > 0; });
    if (!list.length) { box.style.display = 'none'; return; }
    list.forEach(function(am) {
        var icon = amenityIcons[am] || '&#9679;';
        var chip = document.createElement('span');
        chip.className = 'amenity-chip';
        chip.innerHTML = '<span>' + icon + '</span> ' + am;
        chips.appendChild(chip);
    });
    box.style.display = 'block';
}

var roomSel = document.getElementById('roomId');
roomSel.addEventListener('change', function() {
    var opt = this.options[this.selectedIndex];
    showAmenities(opt ? opt.getAttribute('data-amenities') : '');
});
// Show on page load if a room is already pre-selected
if (roomSel.value) {
    var preOpt = roomSel.options[roomSel.selectedIndex];
    showAmenities(preOpt ? preOpt.getAttribute('data-amenities') : '');
}
</script>
</body>
</html>

