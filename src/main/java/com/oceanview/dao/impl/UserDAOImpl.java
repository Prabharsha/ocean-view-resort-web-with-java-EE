package com.oceanview.dao.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.UserDAO;
import com.oceanview.mapper.UserMapper;
import com.oceanview.model.User;
import com.oceanview.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    private final Connection conn;
    private final UserMapper mapper = new UserMapper();

    public UserDAOImpl() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void save(User user) throws DAOException {
        String sql = "INSERT INTO users (id, fname, lname, username, password, role, email, phone, is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        log.debug("save: id={} username='{}' role='{}'", user.getId(), user.getUsername(), user.getRole());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getFname());
            ps.setString(3, user.getLname());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRole());
            ps.setString(7, user.getEmail());
            ps.setString(8, user.getPhone());
            ps.setBoolean(9, user.isActive());
            ps.executeUpdate();
            log.debug("save: user persisted id={}", user.getId());
        } catch (SQLException e) {
            log.error("save: SQL error for username='{}': SQLState={} msg={}", user.getUsername(), e.getSQLState(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) throws DAOException {
        String sql = "UPDATE users SET fname=?, lname=?, username=?, role=?, email=?, phone=?, is_active=? WHERE id=?";
        log.debug("update: id={} username='{}'", user.getId(), user.getUsername());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFname());
            ps.setString(2, user.getLname());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setBoolean(7, user.isActive());
            ps.setString(8, user.getId());
            int rows = ps.executeUpdate();
            log.debug("update: {} row(s) updated for id={}", rows, user.getId());
        } catch (SQLException e) {
            log.error("update: SQL error for id='{}': {}", user.getId(), e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void updateLastLogin(String userId) throws DAOException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id=?";
        log.debug("updateLastLogin: userId={}", userId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("updateLastLogin: SQL error for userId='{}': {}", userId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public User findById(String id) throws DAOException {
        String sql = "SELECT * FROM users WHERE id=?";
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
    public User findByUsername(String username) throws DAOException {
        String sql = "SELECT * FROM users WHERE username=?";
        log.debug("findByUsername: username='{}'", username);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            log.debug("findByUsername: not found for username='{}'", username);
            return null;
        } catch (SQLException e) {
            log.error("findByUsername: SQL error for username='{}': {}", username, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public User findByEmail(String email) throws DAOException {
        String sql = "SELECT * FROM users WHERE email=?";
        log.debug("findByEmail: email='{}'", email);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            return null;
        } catch (SQLException e) {
            log.error("findByEmail: SQL error for email='{}': {}", email, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws DAOException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        log.debug("findAll");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapper.mapRow(rs));
            log.debug("findAll: returned {} user(s)", users.size());
            return users;
        } catch (SQLException e) {
            log.error("findAll: SQL error: {}", e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<User> findByRole(String role) throws DAOException {
        String sql = "SELECT * FROM users WHERE role=? ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        log.debug("findByRole: role='{}'", role);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapper.mapRow(rs));
            log.debug("findByRole: returned {} user(s) for role='{}'", users.size(), role);
            return users;
        } catch (SQLException e) {
            log.error("findByRole: SQL error for role='{}': {}", role, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws DAOException {
        String sql = "DELETE FROM users WHERE id=?";
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
    public void updatePassword(String userId, String hashedPassword) throws DAOException {
        String sql = "UPDATE users SET password=? WHERE id=?";
        log.debug("updatePassword: userId={}", userId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, userId);
            ps.executeUpdate();
            log.debug("updatePassword: password updated for userId={}", userId);
        } catch (SQLException e) {
            log.error("updatePassword: SQL error for userId='{}': {}", userId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void toggleActive(String userId) throws DAOException {
        String sql = "UPDATE users SET is_active = NOT is_active WHERE id=?";
        log.debug("toggleActive: userId={}", userId);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("toggleActive: SQL error for userId='{}': {}", userId, e.getMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }
    }
}

