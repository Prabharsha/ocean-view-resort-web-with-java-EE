package com.oceanview.controller;

import com.oceanview.model.User;
import com.oceanview.service.ReservationService;
import com.oceanview.service.RoomService;
import com.oceanview.service.ServiceException;
import com.oceanview.service.impl.ReservationServiceImpl;
import com.oceanview.service.impl.RoomServiceImpl;
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
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/dashboard")
public class DashboardController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private ReservationService reservationService;
    private RoomService roomService;

    @Override
    public void init() throws ServletException {
        reservationService = new ReservationServiceImpl();
        roomService = new RoomServiceImpl();
        log.info("DashboardController initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getLoggedUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/auth?action=loginForm"); return; }

        log.debug("Loading dashboard for user: {}", user.getUsername());

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            LocalDate today = LocalDate.now();

            // Today's check-ins — include CHECKED_OUT so same-day check-in+out guests count
            int todayCheckIns = countByDateAndStatuses(conn, today, "CHECKED_IN", "CHECKED_OUT");
            req.setAttribute("todayCheckIns", todayCheckIns);

            // Today's check-outs
            int todayCheckOuts = countCheckOuts(conn, today);
            req.setAttribute("todayCheckOuts", todayCheckOuts);

            // Today's reservations
            int todayReservations = countTodayReservations(conn, today);
            req.setAttribute("todayReservations", todayReservations);

            // Available rooms
            int availableRooms = roomService.getAvailableRooms().size();
            req.setAttribute("availableRooms", availableRooms);

            // This month's revenue
            double thisMonthRevenue = getMonthRevenue(conn, today.getYear(), today.getMonthValue());
            req.setAttribute("thisMonthRevenue", thisMonthRevenue);

            // Last month's revenue for comparison
            LocalDate lastMonth = today.minusMonths(1);
            double lastMonthRevenue = getMonthRevenue(conn, lastMonth.getYear(), lastMonth.getMonthValue());
            req.setAttribute("lastMonthRevenue", lastMonthRevenue);

            // Revenue trend percentage
            double revenueTrend = lastMonthRevenue > 0
                ? ((thisMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100 : 0;
            req.setAttribute("revenueTrend", revenueTrend);

            // Room occupancy data from view
            List<Map<String, Object>> occupancy = getOccupancyData(conn);
            req.setAttribute("occupancy", occupancy);

            // Build JSON for Chart.js
            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < occupancy.size(); i++) {
                Map<String, Object> row = occupancy.get(i);
                if (i > 0) jsonBuilder.append(",");
                jsonBuilder.append("{\"type\":\"").append(row.get("room_type"))
                    .append("\",\"total\":").append(row.get("total_rooms"))
                    .append(",\"occupied\":").append(row.get("occupied"))
                    .append(",\"available\":").append(row.get("available"))
                    .append(",\"occupancyPct\":").append(row.get("occupancy_pct"))
                    .append("}");
            }
            jsonBuilder.append("]");
            req.setAttribute("occupancyJson", jsonBuilder.toString());

            // Recent reservations (last 10)
            List<Map<String, Object>> recentReservations = getRecentReservations(conn, 10);
            req.setAttribute("recentReservations", recentReservations);

        } catch (Exception e) {
            log.error("Failed to load dashboard data for user '{}': {}", user.getUsername(), e.getMessage(), e);
            req.setAttribute("error", "Failed to load dashboard data");
        }

        req.getRequestDispatcher("/WEB-INF/views/dashboard/index.jsp").forward(req, resp);
    }

    private int countByDateAndStatus(Connection conn, LocalDate date, String status) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE check_in_date = ? AND status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countByDateAndStatus failed [date={}, status={}]: {}", date, status, e.getMessage(), e);
            return 0;
        }
    }

    private int countByDateAndStatuses(Connection conn, LocalDate date, String... statuses) {
        String placeholders = String.join(",", java.util.Collections.nCopies(statuses.length, "?"));
        String sql = "SELECT COUNT(*) FROM reservations WHERE check_in_date = ? AND status IN (" + placeholders + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            for (int i = 0; i < statuses.length; i++) ps.setString(i + 2, statuses[i]);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countByDateAndStatuses failed [date={}, statuses={}]: {}", date, java.util.Arrays.toString(statuses), e.getMessage(), e);
            return 0;
        }
    }

    private int countCheckOuts(Connection conn, LocalDate date) {
        // Use updated_at (the actual time status changed) rather than check_out_date
        // (the scheduled future date) so early check-outs are counted on the correct day.
        String sql = "SELECT COUNT(*) FROM reservations WHERE DATE(updated_at) = ? AND status = 'CHECKED_OUT'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countCheckOuts failed [date={}]: {}", date, e.getMessage(), e);
            return 0;
        }
    }

    private int countTodayReservations(Connection conn, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE DATE(created_at) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countTodayReservations failed [date={}]: {}", date, e.getMessage(), e);
            return 0;
        }
    }

    private double getMonthRevenue(Connection conn, int year, int month) {
        String sql = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN reservations r ON p.reservation_id = r.id WHERE YEAR(r.check_in_date) = ? AND MONTH(r.check_in_date) = ? AND p.status = 'COMPLETED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            log.error("getMonthRevenue failed [year={}, month={}]: {}", year, month, e.getMessage(), e);
            return 0;
        }
    }

    private List<Map<String, Object>> getOccupancyData(Connection conn) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_room_occupancy";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("getOccupancyData failed - check if view 'vw_room_occupancy' exists: {}", e.getMessage(), e);
        }
        return list;
    }

    private List<Map<String, Object>> getRecentReservations(Connection conn, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_reservation_summary ORDER BY check_in_date DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                list.add(row);
            }
        } catch (SQLException e) {
            log.error("getRecentReservations failed - check if view 'vw_reservation_summary' exists: {}", e.getMessage(), e);
        }
        return list;
    }
}

