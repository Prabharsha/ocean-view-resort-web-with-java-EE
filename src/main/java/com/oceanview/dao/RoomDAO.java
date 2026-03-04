package com.oceanview.dao;

import com.oceanview.model.Room;

import java.time.LocalDate;
import java.util.List;

public interface RoomDAO {
    void save(Room room) throws DAOException;
    void update(Room room) throws DAOException;
    void updateAvailability(String id, boolean available) throws DAOException;
    Room findById(String id) throws DAOException;
    Room findByRoomNumber(String roomNumber) throws DAOException;
    List<Room> findAll() throws DAOException;
    List<Room> findByType(String roomType) throws DAOException;
    List<Room> findAvailable() throws DAOException;
    List<Room> findAvailableByDateRange(LocalDate checkIn, LocalDate checkOut) throws DAOException;
    List<Room> findByFilters(String roomType, Integer floor, LocalDate checkIn, LocalDate checkOut) throws DAOException;
    void delete(String id) throws DAOException;
}

