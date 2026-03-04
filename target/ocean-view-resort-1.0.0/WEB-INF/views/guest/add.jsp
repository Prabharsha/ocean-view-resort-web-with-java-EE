<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Guest — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="Add Guest" /></jsp:include>
        <main class="main-content">
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <div class="page-header"><h2>Add New Guest</h2><a href="${ctx}/guests?action=list" class="btn btn-outline">Back</a></div>
            <div class="card">
                <form action="${ctx}/guests" method="post" class="form-grid">
                    <input type="hidden" name="action" value="${not empty guest.id ? 'update' : 'create'}">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <c:if test="${not empty guest.id}"><input type="hidden" name="id" value="${guest.id}"></c:if>
                    <div class="form-group">
                        <label for="name" class="form-label">Full Name *</label>
                        <input type="text" id="name" name="name" class="form-control" value="${guest.name}" required>
                    </div>
                    <div class="form-group">
                        <label for="email" class="form-label">Email *</label>
                        <input type="email" id="email" name="email" class="form-control" value="${guest.email}" required>
                    </div>
                    <div class="form-group">
                        <label for="contact" class="form-label">Contact Number *</label>
                        <input type="tel" id="contact" name="contact" class="form-control" value="${guest.contact}" required>
                    </div>
                    <div class="form-group">
                        <label for="nic" class="form-label">NIC</label>
                        <input type="text" id="nic" name="nic" class="form-control" value="${guest.nic}">
                    </div>
                    <div class="form-group form-group--full">
                        <label for="address" class="form-label">Address</label>
                        <textarea id="address" name="address" class="form-control" rows="3">${guest.address}</textarea>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">${not empty guest.id ? 'Update' : 'Add'} Guest</button>
                        <a href="${ctx}/guests?action=list" class="btn btn-outline">Cancel</a>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script src="${ctx}/public/js/validation.js"></script>
</body>
</html>

