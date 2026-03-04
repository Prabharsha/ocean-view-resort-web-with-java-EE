<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${not empty editUser}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${isEdit ? 'Edit' : 'Add'} Staff User — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="${isEdit ? 'Edit Staff User' : 'Add Staff User'}" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header">
                <h2>${isEdit ? 'Edit Staff User' : 'Add New Staff User'}</h2>
                <a href="${ctx}/staff" class="btn btn-outline">Back</a>
            </div>
            <div class="card" style="max-width:680px">
                <c:if test="${not isEdit}">
                    <div style="background:#eaf4fb;border:1px solid #b3d9ee;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:13px;color:#1a6080">
                        &#128231; A temporary password will be <strong>auto-generated and emailed</strong> to the staff member.
                    </div>
                </c:if>
                <form action="${ctx}/staff" method="post" class="form-grid">
                    <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <c:if test="${isEdit}"><input type="hidden" name="id" value="${editUser.id}"></c:if>

                    <div class="form-group">
                        <label class="form-label">First Name *</label>
                        <input type="text" name="fname" class="form-control" value="${editUser.fname}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Last Name *</label>
                        <input type="text" name="lname" class="form-control" value="${editUser.lname}" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Username *</label>
                        <input type="text" name="username" class="form-control" value="${editUser.username}" ${isEdit ? 'readonly' : 'required'}>
                        <c:if test="${isEdit}"><small class="form-hint">Username cannot be changed</small></c:if>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Role *</label>
                        <select name="role" class="form-select" required>
                            <option value="">Select Role</option>
                            <option value="STAFF"   ${editUser.role == 'STAFF'   ? 'selected' : ''}>Staff</option>
                            <option value="MANAGER" ${editUser.role == 'MANAGER' ? 'selected' : ''}>Manager</option>
                            <option value="ADMIN"   ${editUser.role == 'ADMIN'   ? 'selected' : ''}>Admin</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Email *</label>
                        <input type="email" name="email" class="form-control" value="${editUser.email}" required>
                        <c:if test="${not isEdit}"><small class="form-hint">Credentials will be sent to this email</small></c:if>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Phone</label>
                        <input type="text" name="phone" class="form-control" value="${editUser.phone}">
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            ${isEdit ? 'Update User' : 'Create &amp; Send Credentials'}
                        </button>
                        <a href="${ctx}/staff" class="btn btn-outline">Cancel</a>
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

