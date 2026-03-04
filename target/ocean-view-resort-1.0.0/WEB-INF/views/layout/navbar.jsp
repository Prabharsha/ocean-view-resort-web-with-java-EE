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
        <%-- Email-sent toast notification (shown once via flash) --%>
        <c:if test="${not empty sessionScope.emailNotice}">
            <div id="emailToast" class="email-toast">
                <span>&#128231;</span> ${sessionScope.emailNotice}
            </div>
            <c:remove var="emailNotice" scope="session"/>
        </c:if>
        <div class="user-badge">
            <a href="${ctx}/profile" class="user-profile-link" title="My Profile">
                <span class="user-name">${sessionScope.loggedUser.fname} ${sessionScope.loggedUser.lname}</span>
                <span class="badge badge-${sessionScope.userRole == 'ADMIN' ? 'danger' : sessionScope.userRole == 'MANAGER' ? 'primary' : 'secondary'}">${sessionScope.userRole}</span>
            </a>
        </div>
        <a href="${ctx}/auth?action=logout" class="btn btn-outline btn-sm">Logout</a>
    </div>
</header>
<style>
.user-profile-link{text-decoration:none;display:flex;align-items:center;gap:8px}
.user-profile-link:hover .user-name{text-decoration:underline;color:var(--secondary)}
.email-toast{
    display:flex;align-items:center;gap:8px;
    padding:8px 16px;border-radius:8px;
    background:#eaf4fb;color:#1a6080;
    border:1px solid #b3d9ee;font-size:13px;font-weight:500;
    animation:slideIn .4s ease,fadeOut .5s ease 4s forwards;
}
@keyframes slideIn{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:translateY(0)}}
@keyframes fadeOut{to{opacity:0;pointer-events:none}}
</style>
