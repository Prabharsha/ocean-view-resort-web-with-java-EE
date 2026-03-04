package com.oceanview.service;

import com.oceanview.model.Reservation;
import com.oceanview.util.ServiceResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    ServiceResult createReservation(Reservation r, String createdByUserId) throws ServiceException;
    ServiceResult updateReservation(Reservation r) throws ServiceException;
    ServiceResult confirmReservation(String id) throws ServiceException;
    ServiceResult cancelReservation(String id) throws ServiceException;
    ServiceResult checkIn(String reservationId, String staffId) throws ServiceException;
    ServiceResult checkOut(String reservationId, String staffId) throws ServiceException;
    Reservation getReservation(String id) throws ServiceException;
    Reservation getByReservationNo(String no) throws ServiceException;
    List<Reservation> getAllReservations() throws ServiceException;
    List<Reservation> getReservationsByStatus(String status) throws ServiceException;
    List<Reservation> getReservationsByGuest(String guestId) throws ServiceException;
    List<Reservation> getReservationsByDateRange(LocalDate from, LocalDate to) throws ServiceException;
    List<Map<String, Object>> getMonthlyReport(int year, int month) throws ServiceException;
    List<Map<String, Object>> getWeeklyReport(LocalDate weekStart) throws ServiceException;
}

