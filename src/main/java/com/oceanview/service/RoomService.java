package com.oceanview.service;

import com.oceanview.model.Room;
import com.oceanview.util.ServiceResult;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {
    ServiceResult addRoom(Room room) throws ServiceException;
    ServiceResult updateRoom(Room room) throws ServiceException;
    ServiceResult deleteRoom(String id) throws ServiceException;
    Room getRoom(String id) throws ServiceException;
    Room getRoomByNumber(String roomNumber) throws ServiceException;
    List<Room> getAllRooms() throws ServiceException;
    List<Room> getAvailableRooms() throws ServiceException;
    List<Room> getAvailableRoomsByDateRange(LocalDate checkIn, LocalDate checkOut) throws ServiceException;
    List<Room> getRoomsByFilters(String roomType, Integer floor, LocalDate checkIn, LocalDate checkOut) throws ServiceException;
    ServiceResult toggleMaintenance(String roomId) throws ServiceException;
}

