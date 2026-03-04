package com.oceanview.controller;

import com.oceanview.model.User;
import com.oceanview.service.ReservationService;
import com.oceanview.service.ServiceException;
import com.oceanview.service.impl.ReservationServiceImpl;
import com.oceanview.util.DBConnection;
import com.oceanview.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/reports")
public class ReportController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private ReservationService reservationService;

    @Override
    public void init() throws ServletException {
        reservationService = new ReservationServiceImpl();
        log.info("ReportController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getLoggedUser(req);
        if (user == null || (!"MANAGER".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            log.warn("Access denied to /reports — user={} role={}",
                    user != null ? user.getUsername() : "anonymous",
                    user != null ? user.getRole() : "none");
            resp.sendError(403, "Access denied — Managers and Admins only");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "dashboard";
        log.debug("GET /reports action={} user={}", action, user.getUsername());
        try {
            switch (action) {
                case "dashboard": handleDashboard(req, resp); break;
                case "monthly": handleMonthly(req, resp); break;
                case "weekly": handleWeekly(req, resp); break;
                case "exportCsv": handleExportCsv(req, resp); break;
                default:
                    log.warn("Unknown GET action '{}' for /reports — falling back to dashboard", action);
                    handleDashboard(req, resp);
            }
        } catch (ServiceException e) {
            log.error("ServiceException in GET /reports action='{}': {}", action, e.getMessage(), e);
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/report/dashboard.jsp").forward(req, resp);
        }
    }

    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        Connection conn = DBConnection.getInstance().getConnection();
        LocalDate today = LocalDate.now();
        log.debug("handleDashboard: loading report data for date={}", today);

        // Today's check-ins — include CHECKED_OUT so same-day check-in+out guests still count
        int todayCheckIns = countQuery(conn, "SELECT COUNT(*) FROM reservations WHERE check_in_date = ? AND status IN ('CHECKED_IN','CONFIRMED','CHECKED_OUT')", today);
        req.setAttribute("todayCheckIns", todayCheckIns);
        log.debug("handleDashboard: todayCheckIns={}", todayCheckIns);

        // This month revenue
        double revenue = doubleQuery(conn, "SELECT COALESCE(SUM(p.amount),0) FROM payments p JOIN reservations r ON p.reservation_id=r.id WHERE YEAR(r.check_in_date)=? AND MONTH(r.check_in_date)=? AND p.status='COMPLETED'", today.getYear(), today.getMonthValue());
        req.setAttribute("thisMonthRevenue", revenue);
        log.debug("handleDashboard: thisMonthRevenue={}", revenue);

        // Room occupancy
        List<Map<String, Object>> occupancy = getViewData(conn, "SELECT * FROM vw_room_occupancy");
        req.setAttribute("occupancy", occupancy);
        log.debug("handleDashboard: occupancy rows={}", occupancy.size());

        req.getRequestDispatcher("/WEB-INF/views/report/dashboard.jsp").forward(req, resp);
    }

    private void handleMonthly(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        int year = intParam(req, "year", LocalDate.now().getYear());
        int month = intParam(req, "month", LocalDate.now().getMonthValue());
        log.debug("handleMonthly: year={} month={}", year, month);

        List<Map<String, Object>> report = reservationService.getMonthlyReport(year, month);
        req.setAttribute("report", report);
        req.setAttribute("year", year);
        req.setAttribute("month", month);
        log.debug("handleMonthly: report rows={}", report.size());

        // Get detailed reservations for the month
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        Connection conn = DBConnection.getInstance().getConnection();
        List<Map<String, Object>> details = getViewData(conn, "SELECT * FROM vw_reservation_summary WHERE check_in_date BETWEEN '" + start + "' AND '" + end + "' ORDER BY check_in_date");
        req.setAttribute("details", details);
        log.debug("handleMonthly: details rows={}", details.size());

        req.getRequestDispatcher("/WEB-INF/views/report/monthly.jsp").forward(req, resp);
    }

    private void handleWeekly(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ServiceException {
        String weekStartStr = req.getParameter("weekStart");
        LocalDate weekStart = (weekStartStr != null && !weekStartStr.isEmpty()) ? LocalDate.parse(weekStartStr) : LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        log.debug("handleWeekly: weekStart={}", weekStart);

        List<Map<String, Object>> report = reservationService.getWeeklyReport(weekStart);
        req.setAttribute("report", report);
        req.setAttribute("weekStart", weekStart.toString());
        log.debug("handleWeekly: report rows={}", report.size());

        // Build JSON for chart
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < report.size(); i++) {
            Map<String, Object> row = report.get(i);
            if (i > 0) json.append(",");
            json.append("{\"day\":\"").append(row.get("day"))
                .append("\",\"reservations\":").append(row.get("reservations"))
                .append(",\"revenue\":").append(row.get("revenue"))
                .append(",\"roomType\":\"").append(row.get("room_type"))
                .append("\",\"roomsOccupied\":").append(row.get("rooms_occupied"))
                .append("}");
        }
        json.append("]");
        req.setAttribute("weeklyJson", json.toString());

        req.getRequestDispatcher("/WEB-INF/views/report/weekly.jsp").forward(req, resp);
    }

    private void handleExportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServiceException {
        String type = req.getParameter("type");
        log.info("handleExportCsv: type={}", type);
        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=\"report.csv\"");
        PrintWriter writer = resp.getWriter();

        if ("monthly".equals(type)) {
            int year = intParam(req, "year", LocalDate.now().getYear());
            int month = intParam(req, "month", LocalDate.now().getMonthValue());
            List<Map<String, Object>> report = reservationService.getMonthlyReport(year, month);
            log.debug("handleExportCsv: monthly year={} month={} rows={}", year, month, report.size());
            if (!report.isEmpty()) {
                Map<String, Object> first = report.get(0);
                writer.println(String.join(",", first.keySet()));
                for (Map<String, Object> row : report) {
                    StringBuilder line = new StringBuilder();
                    int i = 0;
                    for (Object val : row.values()) {
                        if (i > 0) line.append(",");
                        line.append(val != null ? val.toString() : "");
                        i++;
                    }
                    writer.println(line);
                }
            }
        } else if ("weekly".equals(type)) {
            String weekStartStr = req.getParameter("weekStart");
            LocalDate weekStart = weekStartStr != null ? LocalDate.parse(weekStartStr) : LocalDate.now();
            List<Map<String, Object>> report = reservationService.getWeeklyReport(weekStart);
            log.debug("handleExportCsv: weekly weekStart={} rows={}", weekStart, report.size());
            if (!report.isEmpty()) {
                Map<String, Object> first = report.get(0);
                writer.println(String.join(",", first.keySet()));
                for (Map<String, Object> row : report) {
                    StringBuilder line = new StringBuilder();
                    int i = 0;
                    for (Object val : row.values()) {
                        if (i > 0) line.append(",");
                        line.append(val != null ? val.toString() : "");
                        i++;
                    }
                    writer.println(line);
                }
            }
        }
        writer.flush();
    }

    private int countQuery(Connection conn, String sql, LocalDate date) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countQuery failed [date={}] sql='{}': {}", date, sql, e.getMessage(), e);
            return 0;
        }
    }

    private double doubleQuery(Connection conn, String sql, int year, int month) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            log.error("doubleQuery failed [year={}, month={}] sql='{}': {}", year, month, sql, e.getMessage(), e);
            return 0;
        }
    }

    private List<Map<String, Object>> getViewData(Connection conn, String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("getViewData failed sql='{}': {}", sql, e.getMessage(), e);
        }
        return list;
    }

    private int intParam(HttpServletRequest req, String name, int def) {
        try { String v = req.getParameter(name); return v != null ? Integer.parseInt(v) : def; }
        catch (NumberFormatException e) {
            log.warn("intParam: invalid value for parameter '{}', using default {}", name, def);
            return def;
        }
    }
}

