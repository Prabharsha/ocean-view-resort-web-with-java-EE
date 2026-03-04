package com.oceanview.dao.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.mapper.GuestMapper;
import com.oceanview.model.Guest;
import com.oceanview.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestDAOImpl implements GuestDAO {
    private static final Logger log = LoggerFactory.getLogger(GuestDAOImpl.class);

    private final Connection conn;
    private final GuestMapper mapper = new GuestMapper();

    public GuestDAOImpl() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void save(Guest guest) throws DAOException {
        String sql = "INSERT INTO guests (id, name, address, contact, email, nic, loyalty_pts) VALUES (?,?,?,?,?,?,?)";
        log.debug("save: id={} name='{}' email='{}'", guest.getId(), guest.getName(), guest.getEmail());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guest.getId());
            ps.setString(2, guest.getName());
            ps.setString(3, guest.getAddress());
            ps.setString(4, guest.getContact());
            ps.setString(5, guest.getEmail());
            ps.setString(6, guest.getNic());
            ps.setInt(7, guest.getLoyaltyPts());
            ps.executeUpdate();
            log.debug("save: guest persisted id={}", guest.getId());
        } catch (SQLException e) {
            log.error("save: SQL error for guestId='{}': SQLState={} msg={}", guest.getId(), e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Guest guest) throws DAOException {
        String sql = "UPDATE guests SET name=?, address=?, contact=?, email=?, nic=?, loyalty_pts=? WHERE id=?";
        log.debug("update: id={} name='{}'", guest.getId(), guest.getName());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guest.getName());
            ps.setString(2, guest.getAddress());
            ps.setString(3, guest.getContact());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getNic());
            ps.setInt(6, guest.getLoyaltyPts());
            ps.setString(7, guest.getId());
            int rows = ps.executeUpdate();
            log.debug("update: {} row(s) updated for id={}", rows, guest.getId());
        } catch (SQLException e) {
            log.error("update: SQL error for id='{}': {}", guest.getId(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Guest findById(String id) throws DAOException {
        String sql = "SELECT * FROM guests WHERE id=?";
        log.debug("findById: id={}", id);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Guest g = mapper.mapRow(rs);
                log.debug("findById: found name='{}'", g.getName());
                return g;
            }
            log.debug("findById: not found for id={}", id);
            return null;
        } catch (SQLException e) {
            log.error("findById: SQL error for id='{}': {}", id, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Guest findByNic(String nic) throws DAOException {
        String sql = "SELECT * FROM guests WHERE nic=?";
        log.debug("findByNic: nic='{}'", nic);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nic);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            return null;
        } catch (SQLException e) {
            log.error("findByNic: SQL error for nic='{}': {}", nic, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> findAll() throws DAOException {
        String sql = "SELECT * FROM guests ORDER BY created_at DESC";
        List<Guest> guests = new ArrayList<>();
        log.debug("findAll");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) guests.add(mapper.mapRow(rs));
            log.debug("findAll: returned {} guest(s)", guests.size());
            return guests;
        } catch (SQLException e) {
            log.error("findAll: SQL error: {}", e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> search(String keyword) throws DAOException {
        String sql = "SELECT * FROM guests WHERE name LIKE ? OR email LIKE ? OR contact LIKE ? OR nic LIKE ? ORDER BY name";
        List<Guest> guests = new ArrayList<>();
        log.debug("search: keyword='{}'", keyword);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) guests.add(mapper.mapRow(rs));
            log.debug("search: returned {} guest(s) for keyword='{}'", guests.size(), keyword);
            return guests;
        } catch (SQLException e) {
            log.error("search: SQL error for keyword='{}': {}", keyword, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws DAOException {
        String sql = "DELETE FROM guests WHERE id=?";
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
}

