package com.oceanview.controller;

import com.oceanview.util.SessionUtil;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server-Sent Events (SSE) endpoint that tails the application log file in real time.
 * Only accessible to ADMIN users.
 *
 * GET /logs/tail          – SSE stream of new log lines
 * GET /logs               – the viewer UI page
 * GET /logs?action=recent – returns last N lines as plain text (for initial load)
 */
@WebServlet(urlPatterns = {"/logs", "/logs/tail"}, asyncSupported = true)
public class LogTailServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LogTailServlet.class);

    /** How many lines to send on initial connect */
    private static final int INITIAL_LINES = 200;

    /** Poll interval in ms when no new content */
    private static final long POLL_MS = 800;

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "log-tail-thread");
        t.setDaemon(true);
        return t;
    });

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // ADMIN-only guard
        if (!SessionUtil.hasRole(req, "ADMIN")) {
            resp.sendError(403, "Access denied – ADMIN role required");
            return;
        }

        String path = req.getServletPath();

        if ("/logs/tail".equals(path)) {
            streamLogs(req, resp);
        } else {
            // /logs  → show viewer JSP
            String action = req.getParameter("action");
            if ("recent".equals(action)) {
                serveRecentLines(req, resp);
            } else {
                try {
                    req.setAttribute("logFilePath", resolveLogFile());
                    req.getRequestDispatcher("/WEB-INF/views/dashboard/logs.jsp").forward(req, resp);
                } catch (Exception e) {
                    log.error("Failed to forward to logs.jsp", e);
                    resp.sendError(500, e.getMessage());
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // SSE streaming
    // -----------------------------------------------------------------------
    private void streamLogs(HttpServletRequest req, HttpServletResponse resp) {
        File logFile = resolveLogFile();

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("X-Accel-Buffering", "no"); // disable nginx buffering if behind proxy

        AsyncContext async = req.startAsync();
        async.setTimeout(0); // never timeout

        EXECUTOR.submit(() -> {
            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8))) {

                if (!logFile.exists()) {
                    sendEvent(out, "info", "Log file not found yet: " + logFile.getAbsolutePath()
                            + " — it will appear after the first request is made.");
                    out.flush();
                }

                // Send last N lines immediately so the terminal isn't blank
                if (logFile.exists()) {
                    for (String line : tail(logFile, INITIAL_LINES)) {
                        sendEvent(out, "log", escapeHtml(line));
                    }
                    sendEvent(out, "info", "--- live tail started ---");
                    out.flush();
                }

                long filePointer = logFile.exists() ? logFile.length() : 0;

                while (!out.checkError()) {
                    // out.checkError() returns true when client disconnects

                    if (!logFile.exists()) {
                        Thread.sleep(POLL_MS);
                        continue;
                    }

                    long currentLength = logFile.length();

                    if (currentLength < filePointer) {
                        // Log was rotated
                        sendEvent(out, "info", "--- log rotated ---");
                        filePointer = 0;
                    }

                    if (currentLength > filePointer) {
                        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
                            raf.seek(filePointer);
                            String line;
                            while ((line = raf.readLine()) != null) {
                                // readLine on RandomAccessFile uses ISO-8859-1; re-encode
                                line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                                sendEvent(out, "log", escapeHtml(line));
                            }
                            filePointer = raf.getFilePointer();
                        }
                        out.flush();
                    } else {
                        // Send a heartbeat comment so proxies don't close the connection
                        out.print(": heartbeat\n\n");
                        out.flush();
                    }

                    Thread.sleep(POLL_MS);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("SSE log tail error", e);
            } finally {
                async.complete();
            }
        });
    }

    // -----------------------------------------------------------------------
    // Recent lines (plain text, used for non-SSE fallback)
    // -----------------------------------------------------------------------
    private void serveRecentLines(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        File logFile = resolveLogFile();
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        if (!logFile.exists()) {
            out.println("Log file not found: " + logFile.getAbsolutePath());
            return;
        }
        String nParam = req.getParameter("n");
        int n = 500;
        try { if (nParam != null) n = Integer.parseInt(nParam); } catch (NumberFormatException ignored) {}
        for (String line : tail(logFile, n)) out.println(line);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Resolve the log file path — mirrors what logback.xml writes to. */
    public static File resolveLogFile() {
        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null) {
            return new File(catalinaHome, "logs/ocean-view-resort.log");
        }
        // Fallback: look next to catalina.base
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            return new File(catalinaBase, "logs/ocean-view-resort.log");
        }
        // Last resort: temp dir
        return new File(System.getProperty("java.io.tmpdir"), "ocean-view-resort.log");
    }

    /** Read the last {@code n} lines of a file efficiently. */
    private static java.util.List<String> tail(File file, int n) throws IOException {
        java.util.Deque<String> lines = new java.util.ArrayDeque<>(n + 1);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.addLast(line);
                if (lines.size() > n) lines.pollFirst();
            }
        }
        return new java.util.ArrayList<>(lines);
    }

    private static void sendEvent(PrintWriter out, String eventType, String data) {
        out.print("event: " + eventType + "\n");
        // SSE data must not contain raw newlines — split on \n
        for (String part : data.split("\n", -1)) {
            out.print("data: " + part + "\n");
        }
        out.print("\n");
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

