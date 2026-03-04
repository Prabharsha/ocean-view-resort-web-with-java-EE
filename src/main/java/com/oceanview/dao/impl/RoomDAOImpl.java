package com.oceanview.dao.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.RoomDAO;
import com.oceanview.mapper.RoomMapper;
import com.oceanview.model.Room;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {
    private final Connection conn;
    private final RoomMapper mapper = new RoomMapper();

    public RoomDAOImpl() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    @Override
    public void save(Room room) throws DAOException {
        String sql = "INSERT INTO rooms (id, room_number, room_type, floor, capacity, rate_per_night, is_available, description, amenities, image_url) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getRoomType());
            ps.setInt(4, room.getFloor());
            ps.setInt(5, room.getCapacity());
            ps.setDouble(6, room.getRatePerNight());
            ps.setBoolean(7, room.isAvailable());
            ps.setString(8, room.getDescription());
            ps.setString(9, room.getAmenities());
            ps.setString(10, room.getImageUrl());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Room room) throws DAOException {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, floor=?, capacity=?, rate_per_night=?, is_available=?, description=?, amenities=?, image_url=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setInt(3, room.getFloor());
            ps.setInt(4, room.getCapacity());
            ps.setDouble(5, room.getRatePerNight());
            ps.setBoolean(6, room.isAvailable());
            ps.setString(7, room.getDescription());
            ps.setString(8, room.getAmenities());
            ps.setString(9, room.getImageUrl());
            ps.setString(10, room.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void updateAvailability(String id, boolean available) throws DAOException {
        String sql = "UPDATE rooms SET is_available=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Room findById(String id) throws DAOException {
        String sql = "SELECT * FROM rooms WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public Room findByRoomNumber(String roomNumber) throws DAOException {
        String sql = "SELECT * FROM rooms WHERE room_number=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapper.mapRow(rs);
            return null;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAll() throws DAOException {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapper.mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findByType(String roomType) throws DAOException {
        String sql = "SELECT * FROM rooms WHERE room_type=? ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapper.mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAvailable() throws DAOException {
        String sql = "SELECT * FROM rooms WHERE is_available=1 ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rooms.add(mapper.mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findAvailableByDateRange(LocalDate checkIn, LocalDate checkOut) throws DAOException {
        String sql = "SELECT * FROM rooms WHERE is_available=1 AND id NOT IN ("
            + "SELECT room_id FROM reservations WHERE status NOT IN ('CANCELLED','CHECKED_OUT') "
            + "AND check_in_date < ? AND check_out_date > ?) ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapper.mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> findByFilters(String roomType, Integer floor, LocalDate checkIn, LocalDate checkOut) throws DAOException {
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (roomType != null && !roomType.isEmpty()) {
            sql.append(" AND room_type=?");
            params.add(roomType);
        }
        if (floor != null) {
            sql.append(" AND floor=?");
            params.add(floor);
        }
        if (checkIn != null && checkOut != null) {
            sql.append(" AND is_available=1 AND id NOT IN (SELECT room_id FROM reservations WHERE status NOT IN ('CANCELLED','CHECKED_OUT') AND check_in_date < ? AND check_out_date > ?)");
            params.add(Date.valueOf(checkOut));
            params.add(Date.valueOf(checkIn));
        }
        sql.append(" ORDER BY room_number");

        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) ps.setString(i + 1, (String) p);
                else if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else if (p instanceof Date) ps.setDate(i + 1, (Date) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) rooms.add(mapper.mapRow(rs));
            return rooms;
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws DAOException {
        String sql = "DELETE FROM rooms WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }
}

