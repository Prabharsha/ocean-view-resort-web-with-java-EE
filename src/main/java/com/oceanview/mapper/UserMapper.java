package com.oceanview.mapper;

import com.oceanview.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserMapper implements Mapper<User> {
    @Override
    public User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setFname(rs.getString("fname"));
        user.setLname(rs.getString("lname"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) user.setLastLogin(lastLogin.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());
        return user;
    }
}

