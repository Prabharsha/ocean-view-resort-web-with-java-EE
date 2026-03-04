package com.oceanview.service.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.service.ReservationService;
import com.oceanview.service.ServiceException;
import com.oceanview.util.EmailUtil;
import com.oceanview.util.ServiceResult;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationServiceImpl implements ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final GuestDAO guestDAO;

    public ReservationServiceImpl() {
        this.reservationDAO = new ReservationDAOImpl();
        this.roomDAO = new RoomDAOImpl();
        this.guestDAO = new GuestDAOImpl();
    }

    public ReservationServiceImpl(ReservationDAO reservationDAO, RoomDAO roomDAO, GuestDAO guestDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO = roomDAO;
        this.guestDAO = guestDAO;
    }

    @Override
    public ServiceResult createReservation(Reservation r, String createdByUserId) throws ServiceException {
        log.debug("createReservation: guestId={} roomId={} checkIn={} checkOut={} numGuests={} createdBy={}",
                r.getGuestId(), r.getRoomId(), r.getCheckInDate(), r.getCheckOutDate(), r.getNumGuests(), createdByUserId);
        try {
            // 1. Validate all required fields
            if (ValidationUtil.isNullOrEmpty(r.getGuestId()))
                return ServiceResult.failure("Guest is required");
            if (ValidationUtil.isNullOrEmpty(r.getRoomId()))
                return ServiceResult.failure("Room is required");
            if (r.getCheckInDate() == null)
                return ServiceResult.failure("Check-in date required");
            if (r.getCheckOutDate() == null)
                return ServiceResult.failure("Check-out date required");
            if (!r.getCheckOutDate().isAfter(r.getCheckInDate()))
                return ServiceResult.failure("Check-out must be after check-in");
            if (r.getCheckInDate().isBefore(LocalDate.now()))
                return ServiceResult.failure("Check-in cannot be in the past");
            if (r.getNumGuests() < 1)
                return ServiceResult.failure("At least 1 guest required");

            // 2. Capacity check
            Room room = roomDAO.findById(r.getRoomId());
            if (room == null)
                return ServiceResult.failure("Room not found");
            if (r.getNumGuests() > room.getCapacity())
                return ServiceResult.failure("Exceeds room capacity of " + room.getCapacity());

            // 3. Persist (DB trigger handles reservation_no, total_amount, double-booking prevention)
            r.setId(UUID.randomUUID().toString());
            r.setCreatedBy(createdByUserId);
            r.setStatus("PENDING");
            log.debug("createReservation: persisting with id={}", r.getId());
            reservationDAO.save(r);

            // 4. Reload to get trigger-generated reservation_no and total_amount
            Reservation saved = reservationDAO.findById(r.getId());
            log.info("createReservation: reservation saved reservationNo={} totalAmount={}",
                    saved.getReservationNo(), saved.getTotalAmount());

            // 5. Send confirmation email asynchronously
            Guest guest = guestDAO.findById(r.getGuestId());
            if (guest != null && guest.getEmail() != null) {
                log.debug("createReservation: sending confirmation email to {}", guest.getEmail());
                EmailUtil.sendReservationConfirmation(
                    guest.getEmail(), guest.getName(),
                    saved.getReservationNo(),
                    saved.getCheckInDate().toString(),
                    saved.getCheckOutDate().toString(),
                    saved.getTotalAmount());
            } else {
                log.debug("createReservation: no email to send (guest={} email={})",
                        guest != null ? guest.getName() : "null",
                        guest != null ? guest.getEmail() : "null");
            }

            return ServiceResult.success("Reservation " + saved.getReservationNo() + " created successfully", saved);
        } catch (DAOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Room already booked")) {
                log.warn("createReservation: room already booked — guestId={} roomId={} checkIn={} checkOut={}",
                        r.getGuestId(), r.getRoomId(), r.getCheckInDate(), r.getCheckOutDate());
                return ServiceResult.failure("Room is already booked for the selected dates");
            }
            log.error("createReservation DAO error: {}", e.getMessage(), e);
            throw new ServiceException("Failed to create reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult updateReservation(Reservation r) throws ServiceException {
        log.debug("updateReservation: id={} status={}", r.getId(), r.getStatus());
        try {
            if (ValidationUtil.isNullOrEmpty(r.getId()))
                return ServiceResult.failure("Reservation ID is required");

            Reservation existing = reservationDAO.findById(r.getId());
            if (existing == null)
                return ServiceResult.failure("Reservation not found");
            if ("CHECKED_OUT".equals(existing.getStatus()) || "CANCELLED".equals(existing.getStatus()))
                return ServiceResult.failure("Cannot edit a " + existing.getStatus() + " reservation");

            if (r.getCheckInDate() != null && r.getCheckOutDate() != null) {
                if (!r.getCheckOutDate().isAfter(r.getCheckInDate()))
                    return ServiceResult.failure("Check-out must be after check-in");
            }

            reservationDAO.update(r);
            log.info("updateReservation: reservation id={} updated successfully", r.getId());
            return ServiceResult.success("Reservation updated successfully");
        } catch (DAOException e) {
            log.error("updateReservation DAO error for id='{}': {}", r.getId(), e.getMessage(), e);
            throw new ServiceException("Failed to update reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult confirmReservation(String id) throws ServiceException {
        log.debug("confirmReservation: id={}", id);
        try {
            Reservation existing = reservationDAO.findById(id);
            if (existing == null) return ServiceResult.failure("Reservation not found");
            if (!"PENDING".equals(existing.getStatus()))
                return ServiceResult.failure("Only PENDING reservations can be confirmed. Current status: " + existing.getStatus());
            reservationDAO.updateStatus(id, "CONFIRMED");
            log.info("confirmReservation: reservation id={} (no={}) confirmed", id, existing.getReservationNo());
            return ServiceResult.success("Reservation " + existing.getReservationNo() + " confirmed successfully");
        } catch (DAOException e) {
            log.error("confirmReservation DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException("Failed to confirm reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult cancelReservation(String id) throws ServiceException {        log.debug("cancelReservation: id={}", id);
        try {
            Reservation existing = reservationDAO.findById(id);
            if (existing == null) return ServiceResult.failure("Reservation not found");
            if ("CHECKED_OUT".equals(existing.getStatus()))
                return ServiceResult.failure("Cannot cancel a checked-out reservation");
            if ("CANCELLED".equals(existing.getStatus()))
                return ServiceResult.failure("Reservation is already cancelled");

            reservationDAO.updateStatus(id, "CANCELLED");
            log.info("cancelReservation: reservation id={} (no={}) cancelled", id, existing.getReservationNo());
            return ServiceResult.success("Reservation cancelled successfully");
        } catch (DAOException e) {
            log.error("cancelReservation DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException("Failed to cancel reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult checkIn(String reservationId, String staffId) throws ServiceException {
        log.debug("checkIn: reservationId={} staffId={}", reservationId, staffId);
        try {
            Reservation r = reservationDAO.findById(reservationId);
            if (r == null) return ServiceResult.failure("Reservation not found");
            if (!"CONFIRMED".equals(r.getStatus()))
                return ServiceResult.failure("Only CONFIRMED reservations can be checked in. Current status: " + r.getStatus());

            reservationDAO.checkIn(reservationId, staffId);
            log.info("checkIn: reservation id={} (no={}) checked in by staffId={}", reservationId, r.getReservationNo(), staffId);
            return ServiceResult.success("Guest checked in successfully");
        } catch (DAOException e) {
            log.error("checkIn DAO error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new ServiceException("Check-in failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult checkOut(String reservationId, String staffId) throws ServiceException {
        log.debug("checkOut: reservationId={} staffId={}", reservationId, staffId);
        try {
            Reservation r = reservationDAO.findById(reservationId);
            if (r == null) return ServiceResult.failure("Reservation not found");
            if (!"CHECKED_IN".equals(r.getStatus()))
                return ServiceResult.failure("Only CHECKED_IN reservations can be checked out. Current status: " + r.getStatus());

            reservationDAO.checkOut(reservationId, staffId);
            log.info("checkOut: reservation id={} (no={}) checked out by staffId={}", reservationId, r.getReservationNo(), staffId);
            return ServiceResult.success("Guest checked out successfully");
        } catch (DAOException e) {
            log.error("checkOut DAO error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new ServiceException("Check-out failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Reservation getReservation(String id) throws ServiceException {
        log.debug("getReservation: id={}", id);
        try {
            Reservation r = reservationDAO.findById(id);
            if (r == null) log.debug("getReservation: not found for id={}", id);
            return r;
        } catch (DAOException e) {
            log.error("getReservation DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Reservation getByReservationNo(String no) throws ServiceException {
        log.debug("getByReservationNo: no={}", no);
        try {
            return reservationDAO.findByReservationNo(no);
        } catch (DAOException e) {
            log.error("getByReservationNo DAO error for no='{}': {}", no, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getAllReservations() throws ServiceException {
        log.debug("getAllReservations");
        try {
            List<Reservation> list = reservationDAO.findAll();
            log.debug("getAllReservations: returned {} reservation(s)", list.size());
            return list;
        } catch (DAOException e) {
            log.error("getAllReservations DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getReservationsByStatus(String status) throws ServiceException {
        log.debug("getReservationsByStatus: status={}", status);
        try {
            List<Reservation> list = reservationDAO.findByStatus(status);
            log.debug("getReservationsByStatus: returned {} reservation(s) for status={}", list.size(), status);
            return list;
        } catch (DAOException e) {
            log.error("getReservationsByStatus DAO error for status='{}': {}", status, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getReservationsByGuest(String guestId) throws ServiceException {
        log.debug("getReservationsByGuest: guestId={}", guestId);
        try {
            List<Reservation> list = reservationDAO.findByGuestId(guestId);
            log.debug("getReservationsByGuest: returned {} reservation(s) for guestId={}", list.size(), guestId);
            return list;
        } catch (DAOException e) {
            log.error("getReservationsByGuest DAO error for guestId='{}': {}", guestId, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Reservation> getReservationsByDateRange(LocalDate from, LocalDate to) throws ServiceException {
        log.debug("getReservationsByDateRange: from={} to={}", from, to);
        try {
            List<Reservation> list = reservationDAO.findByDateRange(from, to);
            log.debug("getReservationsByDateRange: returned {} reservation(s)", list.size());
            return list;
        } catch (DAOException e) {
            log.error("getReservationsByDateRange DAO error [{} - {}]: {}", from, to, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getMonthlyReport(int year, int month) throws ServiceException {
        log.debug("getMonthlyReport: year={} month={}", year, month);
        try {
            List<Map<String, Object>> report = reservationDAO.getMonthlyReport(year, month);
            log.debug("getMonthlyReport: returned {} row(s)", report.size());
            return report;
        } catch (DAOException e) {
            log.error("getMonthlyReport DAO error [year={} month={}]: {}", year, month, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getWeeklyReport(LocalDate weekStart) throws ServiceException {
        log.debug("getWeeklyReport: weekStart={}", weekStart);
        try {
            List<Map<String, Object>> report = reservationDAO.getWeeklyReport(weekStart);
            log.debug("getWeeklyReport: returned {} row(s)", report.size());
            return report;
        } catch (DAOException e) {
            log.error("getWeeklyReport DAO error [weekStart={}]: {}", weekStart, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}

