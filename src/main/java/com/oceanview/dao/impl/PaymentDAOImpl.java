package com.oceanview.dao.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.PaymentDAO;
import com.oceanview.mapper.PaymentMapper;
import com.oceanview.model.Payment;
import com.oceanview.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {
    private static final Logger log = LoggerFactory.getLogger(PaymentDAOImpl.class);

    private final Connection conn;
    private final PaymentMapper mapper = new PaymentMapper();

    public PaymentDAOImpl() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void save(Payment p) throws DAOException {
        String sql = "INSERT INTO payments (id, reservation_id, amount, payment_method, reference_no, status, processed_by, notes) VALUES (?,?,?,?,?,?,?,?)";
        log.debug("save: id={} reservationId={} amount={} method={}", p.getId(), p.getReservationId(), p.getAmount(), p.getPaymentMethod());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getReservationId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getPaymentMethod());
            ps.setString(5, p.getReferenceNo());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getProcessedBy());
            ps.setString(8, p.getNotes());
            ps.executeUpdate();
            log.debug("save: payment persisted id={}", p.getId());
        } catch (SQLException e) {
            log.error("save: SQL error for paymentId='{}': SQLState={} msg={}", p.getId(), e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Payment p) throws DAOException {
        String sql = "UPDATE payments SET amount=?, payment_method=?, reference_no=?, status=?, notes=? WHERE id=?";
        log.debug("update: id={} status={}", p.getId(), p.getStatus());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, p.getAmount());
            ps.setString(2, p.getPaymentMethod());
            ps.setString(3, p.getReferenceNo());
            ps.setString(4, p.getStatus());
            ps.setString(5, p.getNotes());
            ps.setString(6, p.getId());
            int rows = ps.executeUpdate();
            log.debug("update: {} row(s) updated for id={}", rows, p.getId());
        } catch (SQLException e) {
            log.error("update: SQL error for id='{}': {}", p.getId(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Payment findById(String id) throws DAOException {
        String sql = "SELECT * FROM payments WHERE id=?";
        log.debug("findById: id={}", id);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            log.debug("findById: not found for id={}", id);
            return null;
        } catch (SQLException e) {
            log.error("findById: SQL error for id='{}': {}", id, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Payment findByReservationId(String reservationId) throws DAOException {
        String sql = "SELECT * FROM payments WHERE reservation_id=?";
        log.debug("findByReservationId: reservationId={}", reservationId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            return null;
        } catch (SQLException e) {
            log.error("findByReservationId: SQL error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Payment> findAll() throws DAOException {
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        List<Payment> list = new ArrayList<>();
        log.debug("findAll");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapper.mapRow(rs));
            log.debug("findAll: returned {} payment(s)", list.size());
            return list;
        } catch (SQLException e) {
            log.error("findAll: SQL error: {}", e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsForReservation(String reservationId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM payments WHERE reservation_id=?";
        log.debug("existsForReservation: reservationId={}", reservationId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        } catch (SQLException e) {
            log.error("existsForReservation: SQL error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }
}
