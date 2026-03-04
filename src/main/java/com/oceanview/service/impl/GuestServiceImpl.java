package com.oceanview.service.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.model.Guest;
import com.oceanview.service.GuestService;
import com.oceanview.service.ServiceException;
import com.oceanview.util.ServiceResult;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class GuestServiceImpl implements GuestService {
    private static final Logger log = LoggerFactory.getLogger(GuestServiceImpl.class);

    private final GuestDAO guestDAO;

    public GuestServiceImpl() {
        this.guestDAO = new GuestDAOImpl();
    }

    public GuestServiceImpl(GuestDAO guestDAO) {
        this.guestDAO = guestDAO;
    }

    @Override
    public ServiceResult addGuest(Guest guest) throws ServiceException {
        log.debug("addGuest: name='{}' email='{}' nic='{}'", guest.getName(), guest.getEmail(), guest.getNic());
        try {
            if (ValidationUtil.isNullOrEmpty(guest.getName())) return ServiceResult.failure("Guest name is required");
            if (ValidationUtil.isNullOrEmpty(guest.getContact())) return ServiceResult.failure("Contact number is required");
            if (ValidationUtil.isNullOrEmpty(guest.getEmail())) return ServiceResult.failure("Email is required");
            if (!ValidationUtil.isValidEmail(guest.getEmail())) return ServiceResult.failure("Invalid email format");

            if (guest.getNic() != null && !guest.getNic().isEmpty()) {
                Guest existing = guestDAO.findByNic(guest.getNic());
                if (existing != null) {
                    log.warn("addGuest: NIC already exists '{}'", guest.getNic());
                    return ServiceResult.failure("A guest with this NIC already exists");
                }
            }

            guest.setId(UUID.randomUUID().toString());
            guest.setLoyaltyPts(0);
            guestDAO.save(guest);
            log.info("addGuest: guest saved id={} name='{}'", guest.getId(), guest.getName());
            return ServiceResult.success("Guest added successfully", guest);
        } catch (DAOException e) {
            log.error("addGuest DAO error for name='{}': {}", guest.getName(), e.getMessage(), e);
            throw new ServiceException("Failed to add guest: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult updateGuest(Guest guest) throws ServiceException {
        log.debug("updateGuest: id={} name='{}'", guest.getId(), guest.getName());
        try {
            if (ValidationUtil.isNullOrEmpty(guest.getId())) return ServiceResult.failure("Guest ID is required");
            Guest existing = guestDAO.findById(guest.getId());
            if (existing == null) return ServiceResult.failure("Guest not found");

            if (ValidationUtil.isNullOrEmpty(guest.getName())) return ServiceResult.failure("Guest name is required");
            if (ValidationUtil.isNullOrEmpty(guest.getContact())) return ServiceResult.failure("Contact number is required");
            if (ValidationUtil.isNullOrEmpty(guest.getEmail())) return ServiceResult.failure("Email is required");

            guestDAO.update(guest);
            log.info("updateGuest: guest id={} updated successfully", guest.getId());
            return ServiceResult.success("Guest updated successfully");
        } catch (DAOException e) {
            log.error("updateGuest DAO error for id='{}': {}", guest.getId(), e.getMessage(), e);
            throw new ServiceException("Failed to update guest: " + e.getMessage(), e);
        }
    }

    @Override
    public Guest getGuestById(String id) throws ServiceException {
        log.debug("getGuestById: id={}", id);
        try {
            Guest g = guestDAO.findById(id);
            if (g == null) log.debug("getGuestById: not found for id={}", id);
            return g;
        } catch (DAOException e) {
            log.error("getGuestById DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> getAllGuests() throws ServiceException {
        log.debug("getAllGuests");
        try {
            List<Guest> guests = guestDAO.findAll();
            log.debug("getAllGuests: returned {} guest(s)", guests.size());
            return guests;
        } catch (DAOException e) {
            log.error("getAllGuests DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Guest> searchGuests(String keyword) throws ServiceException {
        log.debug("searchGuests: keyword='{}'", keyword);
        try {
            if (ValidationUtil.isNullOrEmpty(keyword)) return guestDAO.findAll();
            List<Guest> guests = guestDAO.search(keyword);
            log.debug("searchGuests: returned {} guest(s) for keyword='{}'", guests.size(), keyword);
            return guests;
        } catch (DAOException e) {
            log.error("searchGuests DAO error for keyword='{}': {}", keyword, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public ServiceResult deleteGuest(String id) throws ServiceException {
        log.debug("deleteGuest: id={}", id);
        try {
            Guest existing = guestDAO.findById(id);
            if (existing == null) return ServiceResult.failure("Guest not found");
            guestDAO.delete(id);
            log.info("deleteGuest: guest id={} deleted", id);
            return ServiceResult.success("Guest deleted successfully");
        } catch (DAOException e) {
            log.error("deleteGuest DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException("Failed to delete guest: " + e.getMessage(), e);
        }
    }
}

