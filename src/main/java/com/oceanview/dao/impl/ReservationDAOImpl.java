package com.oceanview.dao.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.mapper.ReservationMapper;
import com.oceanview.model.Reservation;
import com.oceanview.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ReservationDAOImpl implements ReservationDAO {
    private static final Logger log = LoggerFactory.getLogger(ReservationDAOImpl.class);

    private final Connection conn;
    private final ReservationMapper mapper = new ReservationMapper();

    public ReservationDAOImpl() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void save(Reservation r) throws DAOException {
        String sql = "INSERT INTO reservations (id, reservation_no, guest_id, room_id, check_in_date, check_out_date, num_guests, status, special_requests, created_by) VALUES (?,?,?,?,?,?,?,?,?,?)";
        log.debug("save: id={} guestId={} roomId={} checkIn={} checkOut={}", r.getId(), r.getGuestId(), r.getRoomId(), r.getCheckInDate(), r.getCheckOutDate());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getReservationNo() != null ? r.getReservationNo() : "TEMP");
            ps.setString(3, r.getGuestId());
            ps.setString(4, r.getRoomId());
            ps.setDate(5, Date.valueOf(r.getCheckInDate()));
            ps.setDate(6, Date.valueOf(r.getCheckOutDate()));
            ps.setInt(7, r.getNumGuests());
            ps.setString(8, r.getStatus());
            ps.setString(9, r.getSpecialRequests());
            ps.setString(10, r.getCreatedBy());
            ps.executeUpdate();
            log.debug("save: reservation persisted id={}", r.getId());
        } catch (SQLException e) {
            log.error("save: SQL error for reservationId='{}': SQLState={} errCode={} msg={}",
                    r.getId(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Reservation r) throws DAOException {
        String sql = "UPDATE reservations SET guest_id=?, room_id=?, check_in_date=?, check_out_date=?, num_guests=?, status=?, special_requests=? WHERE id=?";
        log.debug("update: id={} status={}", r.getId(), r.getStatus());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getGuestId());
            ps.setString(2, r.getRoomId());
            ps.setDate(3, Date.valueOf(r.getCheckInDate()));
            ps.setDate(4, Date.valueOf(r.getCheckOutDate()));
            ps.setInt(5, r.getNumGuests());
            ps.setString(6, r.getStatus());
            ps.setString(7, r.getSpecialRequests());
            ps.setString(8, r.getId());
            int rows = ps.executeUpdate();
            log.debug("update: {} row(s) updated for id={}", rows, r.getId());
        } catch (SQLException e) {
            log.error("update: SQL error for id='{}': SQLState={} errCode={} msg={}",
                    r.getId(), e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void updateStatus(String id, String status) throws DAOException {
        String sql = "UPDATE reservations SET status=? WHERE id=?";
        log.debug("updateStatus: id={} status={}", id, status);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, id);
            int rows = ps.executeUpdate();
            log.debug("updateStatus: {} row(s) updated for id={} to status={}", rows, id, status);
        } catch (SQLException e) {
            log.error("updateStatus: SQL error for id='{}' status='{}': {}", id, status, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws DAOException {
        String sql = "DELETE FROM reservations WHERE id=?";
        log.debug("delete: id={}", id);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            log.debug("delete: {} row(s) deleted for id={}", rows, id);
        } catch (SQLException e) {
            log.error("delete: SQL error for id='{}': {}", id, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Reservation findById(String id) throws DAOException {
        String sql = "SELECT * FROM reservations WHERE id=?";
        log.debug("findById: id={}", id);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservation r = mapper.mapRow(rs);
                log.debug("findById: found reservationNo={}", r.getReservationNo());
                return r;
            }
            log.debug("findById: not found for id={}", id);
            return null;
        } catch (SQLException e) {
            log.error("findById: SQL error for id='{}': {}", id, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Reservation findByReservationNo(String no) throws DAOException {
        String sql = "SELECT * FROM reservations WHERE reservation_no=?";
        log.debug("findByReservationNo: no={}", no);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, no);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            log.debug("findByReservationNo: not found for no={}", no);
            return null;
        } catch (SQLException e) {
            log.error("findByReservationNo: SQL error for no='{}': {}", no, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findAll() throws DAOException {
        String sql = "SELECT * FROM reservations ORDER BY created_at DESC";
        List<Reservation> list = new ArrayList<>();
        log.debug("findAll");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapper.mapRow(rs));
            log.debug("findAll: returned {} reservation(s)", list.size());
            return list;
        } catch (SQLException e) {
            log.error("findAll: SQL error: {}", e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findByStatus(String status) throws DAOException {
        String sql = "SELECT * FROM reservations WHERE status=? ORDER BY created_at DESC";
        List<Reservation> list = new ArrayList<>();
        log.debug("findByStatus: status={}", status);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapper.mapRow(rs));
            log.debug("findByStatus: returned {} reservation(s) for status={}", list.size(), status);
            return list;
        } catch (SQLException e) {
            log.error("findByStatus: SQL error for status='{}': {}", status, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findByGuestId(String guestId) throws DAOException {
        String sql = "SELECT * FROM reservations WHERE guest_id=? ORDER BY created_at DESC";
        List<Reservation> list = new ArrayList<>();
        log.debug("findByGuestId: guestId={}", guestId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guestId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapper.mapRow(rs));
            log.debug("findByGuestId: returned {} reservation(s) for guestId={}", list.size(), guestId);
            return list;
        } catch (SQLException e) {
            log.error("findByGuestId: SQL error for guestId='{}': {}", guestId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate from, LocalDate to) throws DAOException {
        String sql = "SELECT * FROM reservations WHERE check_in_date >= ? AND check_in_date <= ? ORDER BY check_in_date";
        List<Reservation> list = new ArrayList<>();
        log.debug("findByDateRange: from={} to={}", from, to);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapper.mapRow(rs));
            log.debug("findByDateRange: returned {} reservation(s)", list.size());
            return list;
        } catch (SQLException e) {
            log.error("findByDateRange: SQL error [{} - {}]: {}", from, to, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getMonthlyReport(int year, int month) throws DAOException {
        log.debug("getMonthlyReport: calling sp_monthly_report({}, {})", year, month);
        try (CallableStatement cs = conn.prepareCall("{CALL sp_monthly_report(?,?)}")) {
            cs.setInt(1, year);
            cs.setInt(2, month);
            ResultSet rs = cs.executeQuery();
            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                result.add(row);
            }
            log.debug("getMonthlyReport: returned {} row(s)", result.size());
            return result;
        } catch (SQLException e) {
            log.error("getMonthlyReport: SQL error [year={} month={}]: SQLState={} msg={}",
                    year, month, e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getWeeklyReport(LocalDate weekStart) throws DAOException {
        log.debug("getWeeklyReport: calling sp_weekly_report({})", weekStart);
        try (CallableStatement cs = conn.prepareCall("{CALL sp_weekly_report(?)}")) {
            cs.setDate(1, Date.valueOf(weekStart));
            ResultSet rs = cs.executeQuery();
            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                result.add(row);
            }
            log.debug("getWeeklyReport: returned {} row(s)", result.size());
            return result;
        } catch (SQLException e) {
            log.error("getWeeklyReport: SQL error [weekStart={}]: SQLState={} msg={}",
                    weekStart, e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void checkIn(String reservationId, String staffId) throws DAOException {
        log.debug("checkIn: calling sp_check_in({}, {})", reservationId, staffId);
        try (CallableStatement cs = conn.prepareCall("{CALL sp_check_in(?,?)}")) {
            cs.setString(1, reservationId);
            cs.setString(2, staffId);
            cs.execute();
            log.debug("checkIn: sp_check_in executed for reservationId={}", reservationId);
        } catch (SQLException e) {
            log.error("checkIn: SQL error for reservationId='{}': SQLState={} msg={}",
                    reservationId, e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void checkOut(String reservationId, String staffId) throws DAOException {
        log.debug("checkOut: calling sp_check_out({}, {})", reservationId, staffId);
        try (CallableStatement cs = conn.prepareCall("{CALL sp_check_out(?,?)}")) {
            cs.setString(1, reservationId);
            cs.setString(2, staffId);
            cs.execute();
            log.debug("checkOut: sp_check_out executed for reservationId={}", reservationId);
        } catch (SQLException e) {
            log.error("checkOut: SQL error for reservationId='{}': SQLState={} msg={}",
                    reservationId, e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }
}

