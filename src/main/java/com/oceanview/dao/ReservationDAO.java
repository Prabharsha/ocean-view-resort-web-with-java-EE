package com.oceanview.dao;

import com.oceanview.model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReservationDAO {
    void save(Reservation r) throws DAOException;
    void update(Reservation r) throws DAOException;
    void updateStatus(String id, String status) throws DAOException;
    void delete(String id) throws DAOException;
    Reservation findById(String id) throws DAOException;
    Reservation findByReservationNo(String no) throws DAOException;
    List<Reservation> findAll() throws DAOException;
    List<Reservation> findByStatus(String status) throws DAOException;
    List<Reservation> findByGuestId(String guestId) throws DAOException;
    List<Reservation> findByDateRange(LocalDate from, LocalDate to) throws DAOException;
    List<Map<String, Object>> getMonthlyReport(int year, int month) throws DAOException;
    List<Map<String, Object>> getWeeklyReport(LocalDate weekStart) throws DAOException;
    void checkIn(String reservationId, String staffId) throws DAOException;
    void checkOut(String reservationId, String staffId) throws DAOException;
}

