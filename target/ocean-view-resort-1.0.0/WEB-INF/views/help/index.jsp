<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Help &amp; Guidelines — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <style>
        .help-section { max-width: 800px; margin: 32px auto; background: #fff; border-radius: 12px; box-shadow: 0 2px 16px rgba(0,0,0,0.06); padding: 32px 40px; }
        .help-section h1 { font-size: 2.1rem; margin-bottom: 18px; color: #1a6080; }
        .help-section h2 { font-size: 1.3rem; margin-top: 32px; color: #2e86ab; }
        .help-section ul, .help-section ol { margin-left: 24px; }
        .help-section li { margin-bottom: 8px; }
        .help-section .tip { background: #eaf4fb; color: #236e8e; border-left: 4px solid #2e86ab; padding: 8px 16px; border-radius: 6px; margin: 18px 0; font-size: 14px; }
        .help-section code { background: #f4f4f4; color: #236e8e; border-radius: 4px; padding: 2px 6px; font-size: 13px; }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp">
            <jsp:param name="pageTitle" value="Help &amp; FAQ" />
        </jsp:include>
        <main>
            <div class="help-section">
                <h1>Welcome to Ocean View Resort Reservation System</h1>
                <p>This guide will help new staff members get started with the system and perform daily tasks efficiently.</p>
                <h2>1. Logging In</h2>
                <ul>
                    <li>Go to the <b>Login</b> page and enter your username and password.</li>
                    <li>If you forget your password, use the <b>Reset Password</b> option or contact an administrator.</li>
                </ul>
                <h2>2. Dashboard Overview</h2>
                <ul>
                    <li>The dashboard shows today's reservations, check-ins, check-outs, available rooms, and revenue.</li>
                    <li>Recent reservations and room occupancy charts provide a quick overview of current activity.</li>
                </ul>
                <h2>3. Making a Reservation</h2>
                <ol>
                    <li>Navigate to <b>Reservations &gt; New Reservation</b>.</li>
                    <li>Fill in guest details, select room type, check-in and check-out dates.</li>
                    <li>Choose available amenities as needed.</li>
                    <li>Review the summary and click <b>Confirm Reservation</b>.</li>
                    <li>The guest will receive a confirmation email automatically.</li>
                </ol>
                <h2>4. Managing Reservations</h2>
                <ul>
                    <li>View, edit, or cancel reservations from the <b>Reservations</b> tab.</li>
                    <li>Check-in guests on arrival and check-out on departure to update room status.</li>
                    <li>Record payments under the <b>Bills &amp; Payments</b> section.</li>
                </ul>
                <h2>5. Room Management</h2>
                <ul>
                    <li>View all rooms, their status, and amenities under <b>Room Management</b>.</li>
                    <li>Add new rooms or edit existing ones as needed. Select amenities using checkboxes.</li>
                    <li>Set rooms as <b>Available</b> or <b>Unavailable</b> for maintenance.</li>
                </ul>
                <h2>6. Staff &amp; User Management</h2>
                <ul>
                    <li>Admins can add, edit, or deactivate staff accounts under <b>Staff Management</b>.</li>
                    <li>New staff will receive login credentials via email.</li>
                </ul>
                <h2>7. Profile &amp; Password</h2>
                <ul>
                    <li>Click your name in the top right to view or update your profile.</li>
                    <li>Change your password regularly for security.</li>
                </ul>
                <h2>8. Getting Help</h2>
                <ul>
                    <li>If you encounter issues, check this help section or contact your system administrator.</li>
                    <li>For urgent matters, use the <b>Live Logs</b> or <b>Reports</b> section to review system activity.</li>
                </ul>
                <div class="tip">
                    <b>Tip:</b> Always log out after your shift to keep the system secure.
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
</body>
</html>

