<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<header class="navbar">
    <div class="navbar-left">
        <button class="sidebar-toggle" aria-label="Toggle sidebar">
            <span></span><span></span><span></span>
        </button>
        <h2 class="page-title">${param.pageTitle != null ? param.pageTitle : 'Dashboard'}</h2>
    </div>
    <div class="navbar-right">
        <div class="user-badge">
            <span class="user-name">${sessionScope.loggedUser.fname} ${sessionScope.loggedUser.lname}</span>
            <span class="badge badge-${sessionScope.userRole == 'ADMIN' ? 'danger' : sessionScope.userRole == 'MANAGER' ? 'primary' : 'secondary'}">${sessionScope.userRole}</span>
        </div>
        <a href="${ctx}/auth?action=logout" class="btn btn-outline btn-sm">Logout</a>
    </div>
</header>

