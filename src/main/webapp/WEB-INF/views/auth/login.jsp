<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <style>
        /* ── Login Page Override ── */
        body.login-page {
            margin: 0;
            min-height: 100vh;
            background: none;
            display: flex;
            align-items: stretch;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }

        /* ── Left hero panel ── */
        .login-hero {
            flex: 1;
            position: relative;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            padding: 60px 56px;
            overflow: hidden;
            min-height: 100vh;
        }
        .login-hero-bg {
            position: absolute;
            inset: 0;
            background:
                linear-gradient(160deg, #0d2137 0%, #1a3c5e 45%, #2e86ab 100%);
            z-index: 0;
        }
        /* animated floating circles */
        .login-hero-bg::before,
        .login-hero-bg::after {
            content: '';
            position: absolute;
            border-radius: 50%;
            opacity: 0.12;
            animation: floatBubble 8s ease-in-out infinite alternate;
        }
        .login-hero-bg::before {
            width: 520px; height: 520px;
            background: radial-gradient(circle, #f6ae2d, transparent 70%);
            top: -120px; right: -120px;
        }
        .login-hero-bg::after {
            width: 380px; height: 380px;
            background: radial-gradient(circle, #2e86ab, transparent 70%);
            bottom: -80px; left: -80px;
            animation-delay: 3s;
        }
        @keyframes floatBubble {
            from { transform: scale(1) translate(0, 0); }
            to   { transform: scale(1.12) translate(20px, -20px); }
        }

        /* wave SVG strip at bottom of hero */
        .login-hero-wave {
            position: absolute;
            bottom: 0; left: 0; right: 0;
            z-index: 1;
            line-height: 0;
        }

        .login-hero-content {
            position: relative;
            z-index: 2;
            color: #fff;
        }
        .login-hero-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(255,255,255,0.12);
            border: 1px solid rgba(255,255,255,0.2);
            border-radius: 999px;
            padding: 6px 16px;
            font-size: 12px;
            font-weight: 600;
            letter-spacing: 0.8px;
            text-transform: uppercase;
            margin-bottom: 28px;
            backdrop-filter: blur(8px);
        }
        .login-hero-badge span { font-size: 16px; }
        .login-hero-content h2 {
            font-size: clamp(28px, 3vw, 42px);
            font-weight: 800;
            line-height: 1.15;
            margin-bottom: 16px;
            letter-spacing: -0.5px;
        }
        .login-hero-content h2 em {
            font-style: normal;
            color: #f6ae2d;
        }
        .login-hero-content p {
            font-size: 15px;
            color: rgba(255,255,255,0.65);
            max-width: 360px;
            line-height: 1.7;
            margin-bottom: 40px;
        }
        .login-hero-stats {
            display: flex;
            gap: 32px;
        }
        .hero-stat {
            display: flex;
            flex-direction: column;
            gap: 4px;
        }
        .hero-stat-value {
            font-size: 28px;
            font-weight: 800;
            color: #fff;
            line-height: 1;
        }
        .hero-stat-label {
            font-size: 12px;
            color: rgba(255,255,255,0.55);
            letter-spacing: 0.5px;
        }

        /* ── Right form panel ── */
        .login-panel {
            width: 480px;
            flex-shrink: 0;
            background: #f4f7fb;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 48px 40px;
            min-height: 100vh;
            position: relative;
            z-index: 10;
        }
        .login-form-inner {
            width: 100%;
            max-width: 380px;
        }

        /* brand row */
        .login-brand {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 40px;
        }
        .login-brand-icon {
            width: 48px; height: 48px;
            background: linear-gradient(135deg, #1a3c5e, #2e86ab);
            border-radius: 14px;
            display: flex; align-items: center; justify-content: center;
            font-size: 24px;
            box-shadow: 0 4px 12px rgba(26,60,94,0.3);
        }
        .login-brand-text {
            display: flex;
            flex-direction: column;
        }
        .login-brand-text strong {
            font-size: 16px;
            font-weight: 700;
            color: #1a3c5e;
            line-height: 1.2;
        }
        .login-brand-text small {
            font-size: 12px;
            color: #7f8c8d;
        }

        /* heading */
        .login-heading {
            margin-bottom: 32px;
        }
        .login-heading h1 {
            font-size: 26px;
            font-weight: 800;
            color: #1a3c5e;
            margin-bottom: 6px;
            letter-spacing: -0.3px;
        }
        .login-heading p {
            font-size: 14px;
            color: #7f8c8d;
        }

        /* inputs */
        .lf-group {
            margin-bottom: 20px;
            position: relative;
        }
        .lf-label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 7px;
        }
        .lf-input-wrap {
            position: relative;
        }
        .lf-icon {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 16px;
            color: #b0bec5;
            pointer-events: none;
            transition: color 0.2s;
        }
        .lf-input {
            width: 100%;
            padding: 13px 14px 13px 42px;
            border: 2px solid #dde3ea;
            border-radius: 10px;
            font-size: 14px;
            color: #2c3e50;
            background: #fff;
            transition: border-color 0.2s, box-shadow 0.2s;
            font-family: inherit;
            box-sizing: border-box;
        }
        .lf-input::placeholder { color: #b0bec5; }
        .lf-input:focus {
            outline: none;
            border-color: #2e86ab;
            box-shadow: 0 0 0 4px rgba(46,134,171,0.12);
        }
        .lf-input:focus + .lf-icon,
        .lf-input-wrap:focus-within .lf-icon { color: #2e86ab; }

        /* show/hide password toggle */
        .lf-toggle-pw {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            font-size: 16px;
            color: #b0bec5;
            padding: 0;
            line-height: 1;
            transition: color 0.2s;
        }
        .lf-toggle-pw:hover { color: #2e86ab; }
        .lf-input.has-toggle { padding-right: 42px; }

        /* submit btn */
        .lf-submit {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #1a3c5e 0%, #2e86ab 100%);
            color: #fff;
            border: none;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            letter-spacing: 0.3px;
            transition: opacity 0.2s, transform 0.15s, box-shadow 0.2s;
            box-shadow: 0 4px 14px rgba(26,60,94,0.28);
            margin-top: 8px;
            font-family: inherit;
        }
        .lf-submit:hover {
            opacity: 0.92;
            transform: translateY(-1px);
            box-shadow: 0 6px 20px rgba(26,60,94,0.35);
        }
        .lf-submit:active { transform: translateY(0); opacity: 1; }

        /* alert tweaks inside login */
        .lf-alert {
            padding: 12px 16px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 500;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .lf-alert.error   { background: #fde8e8; color: #c0392b; border-left: 4px solid #c0392b; }
        .lf-alert.success { background: #e6f7ed; color: #2d9b5e; border-left: 4px solid #2d9b5e; }

        /* footer */
        .lf-footer {
            margin-top: 36px;
            padding-top: 20px;
            border-top: 1px solid #dde3ea;
            text-align: center;
            font-size: 12px;
            color: #aab4bb;
        }

        /* ── Responsive ── */
        @media (max-width: 900px) {
            .login-hero { display: none; }
            .login-panel {
                width: 100%;
                min-height: 100vh;
                background: linear-gradient(160deg, #0d2137 0%, #1a3c5e 45%, #2e86ab 100%);
            }
            .login-form-inner {
                background: #fff;
                border-radius: 20px;
                padding: 36px 28px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            }
        }
        @media (max-width: 480px) {
            .login-panel { padding: 24px 16px; }
            .login-form-inner { padding: 28px 20px; }
        }
    </style>
</head>
<body class="login-page">

    <!-- ═══════════ LEFT HERO ═══════════ -->
    <div class="login-hero">
        <div class="login-hero-bg"></div>

        <div class="login-hero-wave">
            <svg viewBox="0 0 1440 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M0 40 C240 80 480 0 720 40 C960 80 1200 0 1440 40 L1440 80 L0 80 Z"
                      fill="rgba(255,255,255,0.04)"/>
            </svg>
        </div>

        <div class="login-hero-content">
            <div class="login-hero-badge">
                <span>&#127754;</span> Ocean View Resort
            </div>
            <h2>Welcome to<br><em>Paradise</em> in Galle</h2>
            <p>Manage reservations, guests, and rooms from one elegant dashboard built for hospitality professionals.</p>
            <div class="login-hero-stats">
                <div class="hero-stat">
                    <span class="hero-stat-value">50+</span>
                    <span class="hero-stat-label">Beach Rooms</span>
                </div>
                <div class="hero-stat">
                    <span class="hero-stat-value">24/7</span>
                    <span class="hero-stat-label">Operations</span>
                </div>
                <div class="hero-stat">
                    <span class="hero-stat-value">★ 4.9</span>
                    <span class="hero-stat-label">Guest Rating</span>
                </div>
            </div>
        </div>
    </div>

    <!-- ═══════════ RIGHT FORM PANEL ═══════════ -->
    <div class="login-panel">
        <div class="login-form-inner">

            <!-- brand -->
            <div class="login-brand">
                <div class="login-brand-icon">&#127754;</div>
                <div class="login-brand-text">
                    <strong>Ocean View Resort</strong>
                    <small>Beachside Hotel &mdash; Galle, Sri Lanka</small>
                </div>
            </div>

            <!-- heading -->
            <div class="login-heading">
                <h1>Staff Sign In</h1>
                <p>Enter your credentials to access the dashboard</p>
            </div>

            <!-- alerts -->
            <c:if test="${not empty error}">
                <div class="lf-alert error">
                    <span>&#9888;</span> ${error}
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="lf-alert success">
                    <span>&#10003;</span> ${sessionScope.flashSuccess}
                </div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>

            <!-- form -->
            <form action="${ctx}/auth" method="post" autocomplete="on">
                <input type="hidden" name="action" value="login">

                <div class="lf-group">
                    <label for="username" class="lf-label">Username</label>
                    <div class="lf-input-wrap">
                        <input type="text" id="username" name="username"
                               class="lf-input" placeholder="e.g. john.doe"
                               required autofocus autocomplete="username">
                        <span class="lf-icon">&#128100;</span>
                    </div>
                </div>

                <div class="lf-group">
                    <label for="password" class="lf-label">Password</label>
                    <div class="lf-input-wrap">
                        <input type="password" id="password" name="password"
                               class="lf-input has-toggle" placeholder="Enter your password"
                               required autocomplete="current-password">
                        <span class="lf-icon">&#128274;</span>
                        <button type="button" class="lf-toggle-pw" onclick="togglePw()" title="Show / Hide password">
                            <span id="pw-eye">&#128065;</span>
                        </button>
                    </div>
                </div>

                <button type="submit" class="lf-submit">Sign In &rarr;</button>
            </form>

            <!-- footer -->
            <div class="lf-footer">
                &copy; 2026 Ocean View Resort &bull; Galle, Sri Lanka &bull; All rights reserved
            </div>
        </div>
    </div>

    <script>
        function togglePw() {
            const inp = document.getElementById('password');
            const eye = document.getElementById('pw-eye');
            if (inp.type === 'password') {
                inp.type = 'text';
                eye.textContent = '\uD83D\uDE48'; // 🙈
            } else {
                inp.type = 'password';
                eye.textContent = '\uD83D\uDC41'; // 👁
            }
        }
    </script>
</body>
</html>

