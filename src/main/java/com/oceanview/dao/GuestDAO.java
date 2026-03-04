package com.oceanview.dao;

import com.oceanview.model.Guest;

import java.util.List;

public interface GuestDAO {
    void save(Guest guest) throws DAOException;
    void update(Guest guest) throws DAOException;
    Guest findById(String id) throws DAOException;
    Guest findByNic(String nic) throws DAOException;
    List<Guest> findAll() throws DAOException;
    List<Guest> search(String keyword) throws DAOException;
    void delete(String id) throws DAOException;
}

