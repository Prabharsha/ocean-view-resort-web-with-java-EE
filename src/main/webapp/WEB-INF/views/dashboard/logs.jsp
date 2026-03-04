<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Live Logs — Ocean View Resort</title>
    <link rel="stylesheet" href="${ctx}/public/css/main.css">
    <style>
        /* ── Layout ── */
        .log-page { display: flex; flex-direction: column; height: 100vh; background: #0f1117; color: #d4d4d8; font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace; }

        /* ── Toolbar ── */
        .log-toolbar { display: flex; align-items: center; gap: 12px; padding: 10px 18px;
                       background: #1c1c2e; border-bottom: 1px solid #2d2d44; flex-shrink: 0; }
        .log-toolbar h1 { font-size: 15px; font-weight: 600; color: #a5b4fc; margin: 0; }
        .log-toolbar .badge-live { display: flex; align-items: center; gap: 6px; font-size: 12px;
                                   color: #4ade80; background: #052e16; border: 1px solid #166534;
                                   border-radius: 20px; padding: 2px 10px; }
        .dot { width: 8px; height: 8px; border-radius: 50%; background: #4ade80;
               animation: pulse 1.4s ease-in-out infinite; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.3} }
        .badge-paused { color: #facc15 !important; background: #422006 !important; border-color: #854d0e !important; }
        .badge-paused .dot { background: #facc15 !important; animation: none !important; }
        .badge-error  { color: #f87171 !important; background: #3b0000 !important; border-color: #7f1d1d !important; }
        .badge-error  .dot { background: #f87171 !important; animation: none !important; }

        .spacer { flex: 1; }

        .log-btn { padding: 5px 14px; border-radius: 6px; font-size: 12px; font-weight: 500;
                   cursor: pointer; border: 1px solid; transition: opacity .15s; }
        .log-btn:hover { opacity: .8; }
        .btn-pause  { background: #422006; color: #facc15; border-color: #854d0e; }
        .btn-clear  { background: #1c1c2e; color: #94a3b8; border-color: #334155; }
        .btn-copy   { background: #1c1c2e; color: #94a3b8; border-color: #334155; }
        .btn-scroll { background: #1a2e1a; color: #4ade80; border-color: #166534; }
        .btn-back   { background: #1e1b4b; color: #a5b4fc; border-color: #312e81; text-decoration: none;
                      display: inline-flex; align-items: center; gap: 5px; }

        /* ── Filter bar ── */
        .log-filter { display: flex; align-items: center; gap: 10px; padding: 8px 18px;
                      background: #13131f; border-bottom: 1px solid #2d2d44; flex-shrink: 0; }
        .log-filter input { flex: 1; background: #0f1117; border: 1px solid #334155; color: #e2e8f0;
                            border-radius: 6px; padding: 5px 12px; font-size: 13px; font-family: inherit; }
        .log-filter input::placeholder { color: #64748b; }
        .level-btn { padding: 3px 10px; border-radius: 4px; font-size: 11px; font-weight: 700;
                     cursor: pointer; border: 1px solid transparent; opacity: .45; transition: opacity .15s; }
        .level-btn.active { opacity: 1; }
        .lv-all   { background:#1e293b; color:#94a3b8; border-color:#334155; }
        .lv-debug { background:#1e3a5f; color:#60a5fa; border-color:#1d4ed8; }
        .lv-info  { background:#052e16; color:#4ade80; border-color:#166534; }
        .lv-warn  { background:#422006; color:#facc15; border-color:#854d0e; }
        .lv-error { background:#3b0000; color:#f87171; border-color:#7f1d1d; }
        .filter-count { font-size: 12px; color:#64748b; white-space: nowrap; }

        /* ── Terminal ── */
        .log-terminal { flex: 1; overflow-y: auto; padding: 10px 18px 30px; line-height: 1.55; font-size: 13px; }
        .log-terminal::-webkit-scrollbar { width: 6px; }
        .log-terminal::-webkit-scrollbar-track { background: #0f1117; }
        .log-terminal::-webkit-scrollbar-thumb { background: #334155; border-radius: 3px; }

        .log-line { white-space: pre-wrap; word-break: break-all; padding: 1px 0; }
        .log-line.hidden { display: none; }

        /* Level colours */
        .log-line.lv-DEBUG { color: #60a5fa; }
        .log-line.lv-INFO  { color: #d4d4d8; }
        .log-line.lv-WARN  { color: #facc15; }
        .log-line.lv-ERROR { color: #f87171; font-weight: 600; }
        .log-line.lv-INFO .ts  { color: #6b7280; }
        .log-line.lv-DEBUG .ts { color: #4b5563; }

        /* Highlight search matches */
        mark { background: #854d0e; color: #fef3c7; border-radius: 2px; padding: 0 2px; }

        .log-info-line { color: #7c3aed; font-style: italic; }

        /* ── Status bar ── */
        .log-statusbar { padding: 4px 18px; background: #13131f; border-top: 1px solid #2d2d44;
                         font-size: 11px; color: #64748b; display: flex; gap: 20px; flex-shrink: 0; }
        .log-statusbar span { white-space: nowrap; }
    </style>
</head>
<body class="log-page">

<!-- Toolbar -->
<div class="log-toolbar">
    <a href="${ctx}/dashboard" class="log-btn btn-back">&#8592; Dashboard</a>
    <span>|</span>
    <h1>&#128221; Application Logs</h1>
    <div id="statusBadge" class="badge-live">
        <span class="dot"></span>
        <span id="statusText">LIVE</span>
    </div>
    <div class="spacer"></div>
    <button class="log-btn btn-scroll" id="btnScrollBottom" onclick="scrollBottom()">&#8595; Bottom</button>
    <button class="log-btn btn-pause"  id="btnPause"       onclick="togglePause()">⏸ Pause</button>
    <button class="log-btn btn-clear"  onclick="clearLines()">&#10005; Clear</button>
    <button class="log-btn btn-copy"   onclick="copyAll()">&#128203; Copy All</button>
</div>

<!-- Filter bar -->
<div class="log-filter">
    <input type="text" id="filterInput" placeholder="Filter logs... (supports regex)" oninput="applyFilter()">
    <button class="level-btn lv-all   active" onclick="setLevel('ALL',   this)">ALL</button>
    <button class="level-btn lv-debug"        onclick="setLevel('DEBUG', this)">DEBUG</button>
    <button class="level-btn lv-info"         onclick="setLevel('INFO',  this)">INFO</button>
    <button class="level-btn lv-warn"         onclick="setLevel('WARN',  this)">WARN</button>
    <button class="level-btn lv-error"        onclick="setLevel('ERROR', this)">ERROR</button>
    <span class="filter-count" id="lineCount">0 lines</span>
</div>

<!-- Terminal -->
<div class="log-terminal" id="terminal"></div>

<!-- Status bar -->
<div class="log-statusbar">
    <span id="sbFile">File: <c:out value="${logFilePath}"/></span>
    <span id="sbLines">Total lines: <span id="totalLines">0</span></span>
    <span id="sbConnected">Connected: <span id="connectedAt">—</span></span>
    <span id="sbLast">Last update: <span id="lastUpdate">—</span></span>
</div>

<script>
    const ctx       = '${ctx}';
    const terminal  = document.getElementById('terminal');

    let paused      = false;
    let activeLevel = 'ALL';
    let filterText  = '';
    let filterRegex = null;
    let totalCount  = 0;
    let es          = null;
    let autoScroll  = true;

    // ── Bootstrap: load last lines then start SSE ──────────────────────────
    fetch(ctx + '/logs?action=recent&n=200')
        .then(r => r.text())
        .then(text => {
            text.split('\n').forEach(line => { if (line) appendLine(line, false); });
            scrollBottom();
            startSSE();
        })
        .catch(() => startSSE());

    function startSSE() {
        document.getElementById('connectedAt').textContent = new Date().toLocaleTimeString();
        es = new EventSource(ctx + '/logs/tail');

        es.addEventListener('log', e => {
            if (!paused) appendLine(e.data, true);
        });

        es.addEventListener('info', e => {
            const div = document.createElement('div');
            div.className = 'log-line log-info-line';
            div.textContent = '▶ ' + e.data;
            terminal.appendChild(div);
            if (autoScroll) scrollBottom();
        });

        es.onerror = () => {
            setBadge('error', 'DISCONNECTED');
            // Auto-reconnect after 3s
            setTimeout(() => {
                if (es.readyState === EventSource.CLOSED) {
                    setBadge('live', 'RECONNECTING...');
                    startSSE();
                }
            }, 3000);
        };

        es.onopen = () => setBadge('live', 'LIVE');
    }

    // ── Append a log line ──────────────────────────────────────────────────
    function appendLine(raw, live) {
        totalCount++;
        document.getElementById('totalLines').textContent = totalCount;
        if (live) document.getElementById('lastUpdate').textContent = new Date().toLocaleTimeString();

        const level = detectLevel(raw);
        const html  = highlight(raw);

        const div = document.createElement('div');
        div.className  = 'log-line lv-' + level;
        div.dataset.raw = raw;
        div.dataset.lvl = level;
        div.innerHTML  = html;

        // Apply current filter immediately
        if (!matchesFilter(raw, level)) div.classList.add('hidden');

        terminal.appendChild(div);

        // Keep DOM size bounded (max 5000 lines)
        const lines = terminal.querySelectorAll('.log-line');
        if (lines.length > 5000) lines[0].remove();

        updateCount();
        if (autoScroll && live) scrollBottom();
    }

    // ── Level detection ────────────────────────────────────────────────────
    function detectLevel(line) {
        if (/ ERROR /.test(line)) return 'ERROR';
        if (/ WARN  /.test(line) || / WARN /.test(line)) return 'WARN';
        if (/ INFO  /.test(line) || / INFO /.test(line)) return 'INFO';
        if (/ DEBUG /.test(line)) return 'DEBUG';
        return 'INFO';
    }

    // ── Highlight timestamp and search match ───────────────────────────────
    function highlight(raw) {
        // Escape HTML first
        let s = raw.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');

        // Wrap timestamp (first token yyyy-MM-dd HH:mm:ss.SSS)
        s = s.replace(/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})/, '<span class="ts">$1</span>');

        // Highlight filter match
        if (filterRegex) {
            s = s.replace(filterRegex, m => '<mark>' + m + '</mark>');
        }
        return s;
    }

    // ── Filter ─────────────────────────────────────────────────────────────
    function applyFilter() {
        filterText  = document.getElementById('filterInput').value;
        try {
            filterRegex = filterText ? new RegExp(filterText, 'gi') : null;
        } catch(e) {
            filterRegex = null;
        }
        rerender();
    }

    function setLevel(lvl, btn) {
        activeLevel = lvl;
        document.querySelectorAll('.level-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        rerender();
    }

    function matchesFilter(raw, level) {
        if (activeLevel !== 'ALL' && level !== activeLevel) return false;
        if (!filterText) return true;
        try { return new RegExp(filterText, 'i').test(raw); } catch(e) { return raw.includes(filterText); }
    }

    function rerender() {
        // Rebuild regex with global flag for highlight
        try { filterRegex = filterText ? new RegExp(filterText, 'gi') : null; } catch(e) { filterRegex = null; }

        terminal.querySelectorAll('.log-line').forEach(div => {
            const raw   = div.dataset.raw;
            const level = div.dataset.lvl;
            if (!raw) return; // info lines
            const show = matchesFilter(raw, level);
            div.classList.toggle('hidden', !show);
            if (show) div.innerHTML = highlight(raw);
        });
        updateCount();
    }

    function updateCount() {
        const visible = terminal.querySelectorAll('.log-line:not(.hidden)').length;
        document.getElementById('lineCount').textContent = visible + ' lines';
    }

    // ── Controls ───────────────────────────────────────────────────────────
    function togglePause() {
        paused = !paused;
        const btn = document.getElementById('btnPause');
        if (paused) {
            btn.textContent = '▶ Resume';
            btn.style.background = '#052e16';
            btn.style.color = '#4ade80';
            btn.style.borderColor = '#166534';
            setBadge('paused', 'PAUSED');
        } else {
            btn.textContent = '⏸ Pause';
            btn.style.background = '';
            btn.style.color = '';
            btn.style.borderColor = '';
            setBadge('live', 'LIVE');
        }
    }

    function clearLines() {
        terminal.innerHTML = '';
        totalCount = 0;
        document.getElementById('totalLines').textContent = '0';
        updateCount();
    }

    function copyAll() {
        const lines = [...terminal.querySelectorAll('.log-line:not(.hidden)')]
            .map(d => d.dataset.raw || d.textContent).join('\n');
        navigator.clipboard.writeText(lines).then(() => alert('Copied ' + lines.split('\n').length + ' lines to clipboard'));
    }

    function scrollBottom() {
        terminal.scrollTop = terminal.scrollHeight;
        autoScroll = true;
    }

    // Pause auto-scroll when user scrolls up
    terminal.addEventListener('scroll', () => {
        autoScroll = terminal.scrollTop + terminal.clientHeight >= terminal.scrollHeight - 40;
        document.getElementById('btnScrollBottom').style.display = autoScroll ? 'none' : 'inline-block';
    });

    // ── Badge helper ───────────────────────────────────────────────────────
    function setBadge(state, text) {
        const badge = document.getElementById('statusBadge');
        badge.className = 'badge-' + state;
        document.getElementById('statusText').textContent = text;
    }

    // Keyboard shortcuts
    document.addEventListener('keydown', e => {
        if (e.key === 'p' && !e.target.matches('input')) togglePause();
        if (e.key === 'End') scrollBottom();
        if (e.key === 'Escape') { document.getElementById('filterInput').value = ''; applyFilter(); }
    });
</script>
</body>
</html>

