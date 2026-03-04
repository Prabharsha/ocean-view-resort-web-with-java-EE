package com.oceanview.service.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.model.Room;
import com.oceanview.service.RoomService;
import com.oceanview.service.ServiceException;
import com.oceanview.util.ServiceResult;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RoomServiceImpl implements RoomService {
    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomDAO roomDAO;

    public RoomServiceImpl() {
        this.roomDAO = new RoomDAOImpl();
    }

    public RoomServiceImpl(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    @Override
    public ServiceResult addRoom(Room room) throws ServiceException {
        log.debug("addRoom: number='{}' type='{}' floor={} rate={}", room.getRoomNumber(), room.getRoomType(), room.getFloor(), room.getRatePerNight());
        try {
            if (ValidationUtil.isNullOrEmpty(room.getRoomNumber())) return ServiceResult.failure("Room number is required");
            if (ValidationUtil.isNullOrEmpty(room.getRoomType())) return ServiceResult.failure("Room type is required");
            if (room.getFloor() < 1) return ServiceResult.failure("Floor must be at least 1");
            if (room.getCapacity() < 1) return ServiceResult.failure("Capacity must be at least 1");
            if (room.getRatePerNight() <= 0) return ServiceResult.failure("Rate per night must be positive");

            Room existing = roomDAO.findByRoomNumber(room.getRoomNumber());
            if (existing != null) {
                log.warn("addRoom: room number '{}' already exists", room.getRoomNumber());
                return ServiceResult.failure("Room number already exists");
            }

            room.setId(UUID.randomUUID().toString());
            room.setAvailable(true);
            roomDAO.save(room);
            log.info("addRoom: room saved id={} number='{}'", room.getId(), room.getRoomNumber());
            return ServiceResult.success("Room added successfully", room);
        } catch (DAOException e) {
            log.error("addRoom DAO error for number='{}': {}", room.getRoomNumber(), e.getMessage(), e);
            throw new ServiceException("Failed to add room: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult updateRoom(Room room) throws ServiceException {
        log.debug("updateRoom: id={} number='{}'", room.getId(), room.getRoomNumber());
        try {
            if (ValidationUtil.isNullOrEmpty(room.getId())) return ServiceResult.failure("Room ID is required");
            Room existing = roomDAO.findById(room.getId());
            if (existing == null) return ServiceResult.failure("Room not found");

            if (ValidationUtil.isNullOrEmpty(room.getRoomNumber())) return ServiceResult.failure("Room number is required");
            if (ValidationUtil.isNullOrEmpty(room.getRoomType())) return ServiceResult.failure("Room type is required");
            if (room.getRatePerNight() <= 0) return ServiceResult.failure("Rate per night must be positive");

            roomDAO.update(room);
            log.info("updateRoom: room id={} updated successfully", room.getId());
            return ServiceResult.success("Room updated successfully");
        } catch (DAOException e) {
            log.error("updateRoom DAO error for id='{}': {}", room.getId(), e.getMessage(), e);
            throw new ServiceException("Failed to update room: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult deleteRoom(String id) throws ServiceException {
        log.debug("deleteRoom: id={}", id);
        try {
            Room existing = roomDAO.findById(id);
            if (existing == null) return ServiceResult.failure("Room not found");
            roomDAO.delete(id);
            log.info("deleteRoom: room id={} (number='{}') deleted", id, existing.getRoomNumber());
            return ServiceResult.success("Room deleted successfully");
        } catch (DAOException e) {
            log.error("deleteRoom DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException("Failed to delete room: " + e.getMessage(), e);
        }
    }

    @Override
    public Room getRoom(String id) throws ServiceException {
        log.debug("getRoom: id={}", id);
        try {
            Room r = roomDAO.findById(id);
            if (r == null) log.debug("getRoom: not found for id={}", id);
            return r;
        } catch (DAOException e) {
            log.error("getRoom DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Room getRoomByNumber(String roomNumber) throws ServiceException {
        log.debug("getRoomByNumber: roomNumber='{}'", roomNumber);
        try {
            return roomDAO.findByRoomNumber(roomNumber);
        } catch (DAOException e) {
            log.error("getRoomByNumber DAO error for number='{}': {}", roomNumber, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAllRooms() throws ServiceException {
        log.debug("getAllRooms");
        try {
            List<Room> rooms = roomDAO.findAll();
            log.debug("getAllRooms: returned {} room(s)", rooms.size());
            return rooms;
        } catch (DAOException e) {
            log.error("getAllRooms DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAvailableRooms() throws ServiceException {
        log.debug("getAvailableRooms");
        try {
            List<Room> rooms = roomDAO.findAvailable();
            log.debug("getAvailableRooms: returned {} available room(s)", rooms.size());
            return rooms;
        } catch (DAOException e) {
            log.error("getAvailableRooms DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAvailableRoomsByDateRange(LocalDate checkIn, LocalDate checkOut) throws ServiceException {
        log.debug("getAvailableRoomsByDateRange: checkIn={} checkOut={}", checkIn, checkOut);
        try {
            List<Room> rooms = roomDAO.findAvailableByDateRange(checkIn, checkOut);
            log.debug("getAvailableRoomsByDateRange: returned {} room(s)", rooms.size());
            return rooms;
        } catch (DAOException e) {
            log.error("getAvailableRoomsByDateRange DAO error [{} - {}]: {}", checkIn, checkOut, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getRoomsByFilters(String roomType, Integer floor, LocalDate checkIn, LocalDate checkOut) throws ServiceException {
        log.debug("getRoomsByFilters: type='{}' floor={} checkIn={} checkOut={}", roomType, floor, checkIn, checkOut);
        try {
            List<Room> rooms = roomDAO.findByFilters(roomType, floor, checkIn, checkOut);
            log.debug("getRoomsByFilters: returned {} room(s)", rooms.size());
            return rooms;
        } catch (DAOException e) {
            log.error("getRoomsByFilters DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult toggleMaintenance(String roomId) throws ServiceException {
        log.debug("toggleMaintenance: roomId={}", roomId);
        try {
            Room room = roomDAO.findById(roomId);
            if (room == null) return ServiceResult.failure("Room not found");
            boolean newAvailability = !room.isAvailable();
            roomDAO.updateAvailability(roomId, newAvailability);
            String msg = newAvailability ? "Room marked as available" : "Room marked as unavailable for maintenance";
            log.info("toggleMaintenance: roomId={} number='{}' availability set to {}", roomId, room.getRoomNumber(), newAvailability);
            return ServiceResult.success(msg);
        } catch (DAOException e) {
            log.error("toggleMaintenance DAO error for roomId='{}': {}", roomId, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}

