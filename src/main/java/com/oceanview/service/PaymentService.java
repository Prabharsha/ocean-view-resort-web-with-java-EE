package com.oceanview.service;

import com.oceanview.model.Payment;
import com.oceanview.util.ServiceResult;

import java.util.List;

public interface PaymentService {
    ServiceResult processPayment(String reservationId, String method, String referenceNo, String notes, String staffId) throws ServiceException;
    Payment getPayment(String id) throws ServiceException;
    Payment getPaymentByReservation(String reservationId) throws ServiceException;
    List<Payment> getAllPayments() throws ServiceException;
    boolean isPaymentExists(String reservationId) throws ServiceException;
}

