<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <link rel="stylesheet" href="${ctx}/public/css/dashboard.css">
    <style>
        .profile-layout { display: grid; grid-template-columns: 320px 1fr; gap: 24px; }
        @media(max-width:860px){ .profile-layout { grid-template-columns:1fr; } }

        .profile-avatar-card {
            background: linear-gradient(145deg, #1a3c5e 0%, #2e86ab 100%);
            border-radius: var(--radius); padding: 36px 24px; text-align: center;
            color: #fff; box-shadow: var(--shadow);
        }
        .profile-avatar-circle {
            width: 90px; height: 90px; border-radius: 50%;
            background: rgba(255,255,255,0.2); border: 3px solid rgba(255,255,255,0.5);
            display: flex; align-items: center; justify-content: center;
            font-size: 32px; font-weight: 800; margin: 0 auto 16px; color: #fff;
        }
        .profile-name { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
        .profile-username { font-size: 13px; opacity: .7; margin-bottom: 12px; }
        .profile-role-badge {
            display: inline-block; padding: 4px 14px; border-radius: 999px;
            background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3);
            font-size: 12px; font-weight: 600; letter-spacing: 0.5px;
        }
        .profile-meta { margin-top: 24px; text-align: left; }
        .profile-meta-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.1); font-size: 13px; }
        .profile-meta-row:last-child { border-bottom: none; }
        .profile-meta-row .meta-icon { font-size: 15px; width: 20px; text-align: center; }

        .tab-nav { display: flex; gap: 4px; margin-bottom: 20px; }
        .tab-btn {
            padding: 9px 20px; border-radius: 8px; font-size: 13px; font-weight: 600;
            cursor: pointer; border: none; background: #e8eef4; color: var(--text-muted);
            transition: all .2s;
        }
        .tab-btn.active { background: #2e86ab; color: #fff; }
        .tab-pane { display: none; }
        .tab-pane.active { display: block; }

        .pw-strength { height: 4px; border-radius: 4px; margin-top: 6px; transition: width .3s, background .3s; }
        .pw-strength-text { font-size: 11px; margin-top: 4px; }
    </style>
</head>
<body>
<div class="app-layout">
    <jsp:include page="../layout/sidebar.jsp" />
    <div class="main-wrapper">
        <jsp:include page="../layout/navbar.jsp"><jsp:param name="pageTitle" value="My Profile" /></jsp:include>
        <main class="main-content">

            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert success">${sessionScope.flashSuccess}</div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert error">${sessionScope.flashError}</div>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <div class="profile-layout">
                <!-- ── Left: Avatar card ── -->
                <div>
                    <div class="profile-avatar-card">
                        <div class="profile-avatar-circle">
                            ${fn:substring(profileUser.fname,0,1)}${fn:substring(profileUser.lname,0,1)}
                        </div>
                        <div class="profile-name">${profileUser.fname} ${profileUser.lname}</div>
                        <div class="profile-username">@${profileUser.username}</div>
                        <div class="profile-role-badge">${profileUser.role}</div>
                        <div class="profile-meta">
                            <div class="profile-meta-row">
                                <span class="meta-icon">&#128231;</span>
                                <span>${not empty profileUser.email ? profileUser.email : '—'}</span>
                            </div>
                            <div class="profile-meta-row">
                                <span class="meta-icon">&#128222;</span>
                                <span>${not empty profileUser.phone ? profileUser.phone : '—'}</span>
                            </div>
                            <div class="profile-meta-row">
                                <span class="meta-icon">&#128336;</span>
                                <span>Last login: ${not empty profileUser.lastLogin ? profileUser.lastLogin : '—'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ── Right: Tabs ── -->
                <div>
                    <div class="tab-nav">
                        <button class="tab-btn active" onclick="switchTab('info', this)">&#128100; Profile Info</button>
                        <button class="tab-btn" onclick="switchTab('password', this)">&#128274; Change Password</button>
                    </div>

                    <!-- Profile Info tab -->
                    <div id="tab-info" class="tab-pane active">
                        <div class="card">
                            <h3 class="card-title">Edit Profile Information</h3>
                            <form action="${ctx}/profile" method="post" class="form-grid">
                                <input type="hidden" name="action" value="updateProfile">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <div class="form-group">
                                    <label class="form-label">First Name</label>
                                    <input type="text" name="fname" class="form-control" value="${profileUser.fname}" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Last Name</label>
                                    <input type="text" name="lname" class="form-control" value="${profileUser.lname}" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Email</label>
                                    <input type="email" name="email" class="form-control" value="${profileUser.email}" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Phone</label>
                                    <input type="text" name="phone" class="form-control" value="${profileUser.phone}">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Username</label>
                                    <input type="text" class="form-control" value="${profileUser.username}" disabled>
                                    <small class="form-hint">Username cannot be changed</small>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Role</label>
                                    <input type="text" class="form-control" value="${profileUser.role}" disabled>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">&#10003; Save Changes</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Change Password tab -->
                    <div id="tab-password" class="tab-pane">
                        <div class="card">
                            <h3 class="card-title">Change Password</h3>
                            <c:if test="${not empty pwError}">
                                <div class="alert error">${pwError}</div>
                            </c:if>
                            <form action="${ctx}/profile" method="post" class="form-grid" id="pwForm">
                                <input type="hidden" name="action" value="changePassword">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <div class="form-group form-group--full">
                                    <label class="form-label">Current Password *</label>
                                    <input type="password" name="currentPassword" id="currentPw" class="form-control" required autocomplete="current-password">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">New Password *</label>
                                    <input type="password" name="newPassword" id="newPw" class="form-control" required autocomplete="new-password" oninput="checkStrength(this.value)">
                                    <div class="pw-strength" id="pwStrengthBar" style="width:0;background:#e8eef4"></div>
                                    <div class="pw-strength-text text-muted" id="pwStrengthText"></div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Confirm New Password *</label>
                                    <input type="password" name="confirmPassword" id="confirmPw" class="form-control" required autocomplete="new-password" oninput="checkMatch()">
                                    <div class="pw-strength-text" id="matchText"></div>
                                </div>
                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">&#128274; Update Password</button>
                                </div>
                            </form>
                        </div>
                        <div class="card" style="margin-top:16px;background:#fff8e1;border-left:4px solid #f6ae2d">
                            <p style="font-size:13px;color:#7c6000;margin:0">
                                &#9888; After changing your password you will be logged out and need to sign in again with the new password.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<script>var contextPath='${ctx}';</script>
<script src="${ctx}/public/js/main.js"></script>
<script>
function switchTab(name, btn) {
    document.querySelectorAll('.tab-pane').forEach(function(p){ p.classList.remove('active'); });
    document.querySelectorAll('.tab-btn').forEach(function(b){ b.classList.remove('active'); });
    document.getElementById('tab-' + name).classList.add('active');
    btn.classList.add('active');
}
// Open password tab if there's a pwError
<c:if test="${not empty pwError}">
switchTab('password', document.querySelectorAll('.tab-btn')[1]);
</c:if>

function checkStrength(pw) {
    var bar = document.getElementById('pwStrengthBar');
    var txt = document.getElementById('pwStrengthText');
    var score = 0;
    if (pw.length >= 6) score++;
    if (pw.length >= 10) score++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    var colors = ['#e74c3c','#e67e22','#f6ae2d','#2d9b5e','#2e86ab'];
    var labels = ['Very Weak','Weak','Fair','Strong','Very Strong'];
    var widths = ['20%','40%','60%','80%','100%'];
    bar.style.width  = pw.length ? widths[score-1]||'20%' : '0';
    bar.style.background = pw.length ? colors[score-1]||'#e8eef4' : '#e8eef4';
    txt.textContent  = pw.length ? labels[score-1]||'' : '';
    txt.style.color  = pw.length ? colors[score-1]||'' : '';
}
function checkMatch() {
    var np = document.getElementById('newPw').value;
    var cp = document.getElementById('confirmPw').value;
    var el = document.getElementById('matchText');
    if (!cp) { el.textContent=''; return; }
    if (np === cp) { el.textContent='✓ Passwords match'; el.style.color='#2d9b5e'; }
    else           { el.textContent='✗ Passwords do not match'; el.style.color='#e74c3c'; }
}
</script>
</body>
</html>

