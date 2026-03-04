package com.oceanview.service;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock private RoomDAO roomDAO;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roomService = new RoomServiceImpl(roomDAO);
    }

    @Test
    @DisplayName("Should return null when room not found")
    void getRoom_notFound_returnsNull() throws Exception {
        when(roomDAO.findById("nonexistent")).thenReturn(null);
        Room room = roomService.getRoom("nonexistent");
        assertNull(room);
    }

    @Test
    @DisplayName("Should fail when room number is empty")
    void addRoom_noRoomNumber_fails() throws Exception {
        Room room = new Room();
        room.setRoomNumber("");
        room.setRoomType("STANDARD");
        room.setFloor(1);
        room.setCapacity(2);
        room.setRatePerNight(8500);

        ServiceResult result = roomService.addRoom(room);
        assertFalse(result.isSuccess());
        assertEquals("Room number is required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when room type is empty")
    void addRoom_noRoomType_fails() throws Exception {
        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType("");
        room.setFloor(1);
        room.setCapacity(2);
        room.setRatePerNight(8500);

        ServiceResult result = roomService.addRoom(room);
        assertFalse(result.isSuccess());
        assertEquals("Room type is required", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when rate is zero or negative")
    void addRoom_invalidRate_fails() throws Exception {
        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType("STANDARD");
        room.setFloor(1);
        room.setCapacity(2);
        room.setRatePerNight(0);

        ServiceResult result = roomService.addRoom(room);
        assertFalse(result.isSuccess());
        assertEquals("Rate per night must be positive", result.getMessage());
    }

    @Test
    @DisplayName("Should fail when room number already exists")
    void addRoom_duplicateNumber_fails() throws Exception {
        Room existing = new Room();
        existing.setId("existing-id");
        existing.setRoomNumber("101");

        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType("STANDARD");
        room.setFloor(1);
        room.setCapacity(2);
        room.setRatePerNight(8500);

        when(roomDAO.findByRoomNumber("101")).thenReturn(existing);

        ServiceResult result = roomService.addRoom(room);
        assertFalse(result.isSuccess());
        assertEquals("Room number already exists", result.getMessage());
    }

    @Test
    @DisplayName("Should succeed when adding a valid room")
    void addRoom_valid_succeeds() throws Exception {
        Room room = new Room();
        room.setRoomNumber("501");
        room.setRoomType("SUITE");
        room.setFloor(5);
        room.setCapacity(4);
        room.setRatePerNight(25000);

        when(roomDAO.findByRoomNumber("501")).thenReturn(null);

        ServiceResult result = roomService.addRoom(room);
        assertTrue(result.isSuccess());
        assertEquals("Room added successfully", result.getMessage());
        verify(roomDAO).save(any(Room.class));
    }

    @Test
    @DisplayName("Should fail when deleting non-existent room")
    void deleteRoom_notFound_fails() throws Exception {
        when(roomDAO.findById("fake-id")).thenReturn(null);

        ServiceResult result = roomService.deleteRoom("fake-id");
        assertFalse(result.isSuccess());
        assertEquals("Room not found", result.getMessage());
    }

    @Test
    @DisplayName("Should toggle room maintenance correctly")
    void toggleMaintenance_available_makesUnavailable() throws Exception {
        Room room = new Room();
        room.setId("room-001");
        room.setAvailable(true);
        when(roomDAO.findById("room-001")).thenReturn(room);

        ServiceResult result = roomService.toggleMaintenance("room-001");
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("unavailable"));
        verify(roomDAO).updateAvailability("room-001", false);
    }
}

