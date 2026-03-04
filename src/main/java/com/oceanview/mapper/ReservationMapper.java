package com.oceanview.mapper;

import com.oceanview.model.Reservation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReservationMapper implements Mapper<Reservation> {
    @Override
    public Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getString("id"));
        r.setReservationNo(rs.getString("reservation_no"));
        r.setGuestId(rs.getString("guest_id"));
        r.setRoomId(rs.getString("room_id"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setNumGuests(rs.getInt("num_guests"));
        r.setStatus(rs.getString("status"));
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setCreatedBy(rs.getString("created_by"));
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) r.setUpdatedAt(updatedAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) r.setCreatedAt(createdAt.toLocalDateTime());
        return r;
    }
}

