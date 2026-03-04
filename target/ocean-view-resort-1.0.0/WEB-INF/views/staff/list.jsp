<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Management — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <style>
        .role-badge-ADMIN{background:#fde8e8;color:#c0392b;border:1px solid #f5b7b1}
        .role-badge-MANAGER{background:#dbeafe;color:#1d4ed8;border:1px solid #bfdbfe}
        .role-badge-STAFF{background:#dcfce7;color:#16a34a;border:1px solid #bbf7d0}
        .role-chip{display:inline-block;padding:3px 10px;border-radius:999px;font-size:11px;font-weight:700}
        .status-active{color:#16a34a;font-weight:600}
        .status-inactive{color:#c0392b;font-weight:600}
        .modal-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,.45);z-index:9000;align-items:center;justify-content:center}
        .modal-overlay.open{display:flex}
        .modal-box{background:#fff;border-radius:12px;padding:28px;width:360px;box-shadow:0 8px 32px rgba(0,0,0,.18)}
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Staff Management" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty sessionScope.flashSuccess}"><div class="alert success">${sessionScope.flashSuccess}</div><c:remove var="flashSuccess" scope="session"/></c:if>
            <c:if test="${not empty sessionScope.flashError}"><div class="alert error">${sessionScope.flashError}</div><c:remove var="flashError" scope="session"/></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header">
                <h2>Staff Users</h2>
                <a href="${ctx}/staff?action=new" class="btn btn-primary">+ Add Staff User</a>
            </div>
            <div class="card">
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr><th>Name</th><th>Username</th><th>Email</th><th>Phone</th><th>Role</th><th>Status</th><th>Last Login</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="u" items="${staffUsers}">
                            <tr>
                                <td><strong>${u.fname} ${u.lname}</strong></td>
                                <td><code style="font-size:12px">${u.username}</code></td>
                                <td>${u.email}</td>
                                <td>${not empty u.phone ? u.phone : '-'}</td>
                                <td><span class="role-chip role-badge-${u.role}">${u.role}</span></td>
                                <td><c:choose><c:when test="${u.active}"><span class="status-active">&#9679; Active</span></c:when><c:otherwise><span class="status-inactive">&#9679; Inactive</span></c:otherwise></c:choose></td>
                                <td style="font-size:12px;color:var(--text-muted)">${not empty u.lastLogin ? u.lastLogin : 'Never'}</td>
                                <td class="actions-cell">
                                    <a href="${ctx}/staff?action=edit&id=${u.id}" class="btn btn-sm btn-action-edit">Edit</a>
                                    <form action="${ctx}/staff" method="post" style="display:inline">
                                        <input type="hidden" name="action" value="toggle">
                                        <input type="hidden" name="id" value="${u.id}">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <button type="submit" class="btn btn-sm ${u.active ? 'btn-action-delete' : 'btn-action-view'}" data-confirm="${u.active ? 'Deactivate' : 'Activate'} ${u.username}?">${u.active ? 'Deactivate' : 'Activate'}</button>
                                    </form>
                                    <button class="btn btn-sm btn-action-view" onclick="openResetModal('${u.id}','${u.fname} ${u.lname}')">Reset PW</button>
                                    <c:if test="${sessionScope.loggedUser.id != u.id}">
                                        <form action="${ctx}/staff" method="post" style="display:inline">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <button type="submit" class="btn btn-sm btn-action-delete" data-confirm="Delete ${u.username}?">Delete</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty staffUsers}"><tr><td colspan="8" class="text-center text-muted">No staff users found</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="modal-overlay" id="resetPwModal">
                <div class="modal-box">
                    <h3 style="margin:0 0 8px;color:var(--primary)">Reset Password</h3>
                    <p id="resetPwName" style="font-size:13px;color:var(--text-muted);margin:0 0 16px"></p>
                    <form action="${ctx}/staff" method="post">
                        <input type="hidden" name="action" value="resetPassword">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="id" id="resetPwId">
                        <div class="form-group"><label class="form-label">New Password *</label><input type="password" name="newPassword" class="form-control" minlength="6" required></div>
                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">Reset</button>
                            <button type="button" class="btn btn-outline" onclick="closeResetModal()">Cancel</button>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script>
function openResetModal(id,name){document.getElementById('resetPwId').value=id;document.getElementById('resetPwName').textContent='User: '+name;document.getElementById('resetPwModal').classList.add('open');}
function closeResetModal(){document.getElementById('resetPwModal').classList.remove('open');}
document.getElementById('resetPwModal').addEventListener('click',function(e){if(e.target===this)closeResetModal();});
</script>
</body>
</html>

