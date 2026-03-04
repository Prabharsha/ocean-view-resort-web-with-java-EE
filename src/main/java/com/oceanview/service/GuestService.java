package com.oceanview.service;

import com.oceanview.model.Guest;
import com.oceanview.util.ServiceResult;

import java.util.List;

public interface GuestService {
    ServiceResult addGuest(Guest guest) throws ServiceException;
    ServiceResult updateGuest(Guest guest) throws ServiceException;
    Guest getGuestById(String id) throws ServiceException;
    List<Guest> getAllGuests() throws ServiceException;
    List<Guest> searchGuests(String keyword) throws ServiceException;
    ServiceResult deleteGuest(String id) throws ServiceException;
}

