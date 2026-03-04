package com.oceanview.dao;

import com.oceanview.model.Payment;

import java.util.List;

public interface PaymentDAO {
    void save(Payment p) throws DAOException;
    void update(Payment p) throws DAOException;
    Payment findById(String id) throws DAOException;
    Payment findByReservationId(String reservationId) throws DAOException;
    List<Payment> findAll() throws DAOException;
    boolean existsForReservation(String reservationId) throws DAOException;
}

