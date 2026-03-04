package com.oceanview.mapper;

import com.oceanview.model.Guest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class GuestMapper implements Mapper<Guest> {
    @Override
    public Guest mapRow(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getString("id"));
        guest.setName(rs.getString("name"));
        guest.setAddress(rs.getString("address"));
        guest.setContact(rs.getString("contact"));
        guest.setEmail(rs.getString("email"));
        guest.setNic(rs.getString("nic"));
        guest.setLoyaltyPts(rs.getInt("loyalty_pts"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) guest.setCreatedAt(createdAt.toLocalDateTime());
        return guest;
    }
}

