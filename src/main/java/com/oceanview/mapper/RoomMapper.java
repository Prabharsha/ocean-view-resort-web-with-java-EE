package com.oceanview.mapper;

import com.oceanview.model.Room;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomMapper implements Mapper<Room> {
    @Override
    public Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getString("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(rs.getString("room_type"));
        room.setFloor(rs.getInt("floor"));
        room.setCapacity(rs.getInt("capacity"));
        room.setRatePerNight(rs.getDouble("rate_per_night"));
        room.setAvailable(rs.getBoolean("is_available"));
        room.setDescription(rs.getString("description"));
        room.setAmenities(rs.getString("amenities"));
        room.setImageUrl(rs.getString("image_url"));
        return room;
    }
}

