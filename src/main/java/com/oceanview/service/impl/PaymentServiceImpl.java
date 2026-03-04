package com.oceanview.service.impl;

import com.oceanview.dao.DAOException;
import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.PaymentDAO;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.dao.impl.PaymentDAOImpl;
import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.model.Guest;
import com.oceanview.model.Payment;
import com.oceanview.model.Reservation;
import com.oceanview.payment.CardPaymentStrategy;
import com.oceanview.payment.CashPaymentStrategy;
import com.oceanview.payment.PaymentContext;
import com.oceanview.service.PaymentService;
import com.oceanview.service.ServiceException;
import com.oceanview.util.EmailUtil;
import com.oceanview.util.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentDAO paymentDAO;
    private final ReservationDAO reservationDAO;
    private final GuestDAO guestDAO;

    public PaymentServiceImpl() {
        this.paymentDAO = new PaymentDAOImpl();
        this.reservationDAO = new ReservationDAOImpl();
        this.guestDAO = new GuestDAOImpl();
    }

    public PaymentServiceImpl(PaymentDAO paymentDAO, ReservationDAO reservationDAO, GuestDAO guestDAO) {
        this.paymentDAO = paymentDAO;
        this.reservationDAO = reservationDAO;
        this.guestDAO = guestDAO;
    }

    @Override
    public ServiceResult processPayment(String reservationId, String method,
                                        String referenceNo, String notes, String staffId) throws ServiceException {
        log.debug("processPayment: reservationId={} method={} referenceNo={} staffId={}", reservationId, method, referenceNo, staffId);
        try {
            // 1. Load and validate reservation
            Reservation r = reservationDAO.findById(reservationId);
            if (r == null) {
                log.warn("processPayment: reservation not found for id={}", reservationId);
                return ServiceResult.failure("Reservation not found");
            }
            if ("CHECKED_OUT".equals(r.getStatus()) || "CANCELLED".equals(r.getStatus())) {
                log.warn("processPayment: cannot process — reservation {} is {}", reservationId, r.getStatus());
                return ServiceResult.failure("Cannot process payment — reservation is " + r.getStatus());
            }

            // 2. Prevent duplicate payment
            if (paymentDAO.existsForReservation(reservationId)) {
                log.warn("processPayment: duplicate payment attempt for reservationId={}", reservationId);
                return ServiceResult.failure("A payment has already been recorded for this reservation");
            }

            // 3. Validate method
            if (!"CASH".equals(method) && !"CARD".equals(method)) {
                log.warn("processPayment: invalid payment method '{}' for reservationId={}", method, reservationId);
                return ServiceResult.failure("Payment method must be CASH or CARD");
            }

            // 4. Validate reference number
            if (referenceNo == null || referenceNo.trim().isEmpty()) {
                log.warn("processPayment: missing reference number for reservationId={} method={}", reservationId, method);
                return ServiceResult.failure("Reference number is required. "
                    + ("CASH".equals(method) ? "Enter the cash receipt number." : "Enter the card terminal transaction ID."));
            }

            // 5. Strategy pattern selects method-specific validation
            PaymentContext ctx = new PaymentContext();
            ctx.setStrategy("CASH".equals(method) ? new CashPaymentStrategy() : new CardPaymentStrategy());
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setReservationId(reservationId);
            payment.setAmount(r.getTotalAmount());
            payment.setPaymentMethod(method);
            payment.setReferenceNo(referenceNo.trim());
            payment.setProcessedBy(staffId);
            payment.setStatus("COMPLETED");
            payment.setNotes(notes);

            ServiceResult strategyResult = ctx.executePayment(payment);
            if (!strategyResult.isSuccess()) {
                log.warn("processPayment: strategy validation failed for reservationId={}: {}", reservationId, strategyResult.getMessage());
                return strategyResult;
            }

            // 6. Save payment record
            paymentDAO.save(payment);
            log.info("processPayment: payment saved id={} amount={} method={} reservationId={}", payment.getId(), payment.getAmount(), method, reservationId);

            // 7. Send payment receipt email to guest
            Guest guest = guestDAO.findById(r.getGuestId());
            if (guest != null && guest.getEmail() != null) {
                log.debug("processPayment: sending receipt email to {}", guest.getEmail());
                EmailUtil.sendPaymentReceipt(
                    guest.getEmail(), guest.getName(),
                    r.getReservationNo(), method, referenceNo, payment.getAmount());
            } else {
                log.debug("processPayment: no receipt email sent (guest={} email={})",
                        guest != null ? guest.getName() : "null",
                        guest != null ? guest.getEmail() : "null");
            }

            return ServiceResult.success(
                "Payment of LKR " + String.format("%.2f", payment.getAmount()) + " recorded successfully", payment);
        } catch (DAOException e) {
            log.error("processPayment DAO error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new ServiceException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment getPayment(String id) throws ServiceException {
        log.debug("getPayment: id={}", id);
        try {
            Payment p = paymentDAO.findById(id);
            if (p == null) log.debug("getPayment: not found for id={}", id);
            return p;
        } catch (DAOException e) {
            log.error("getPayment DAO error for id='{}': {}", id, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Payment getPaymentByReservation(String reservationId) throws ServiceException {
        log.debug("getPaymentByReservation: reservationId={}", reservationId);
        try {
            return paymentDAO.findByReservationId(reservationId);
        } catch (DAOException e) {
            log.error("getPaymentByReservation DAO error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Payment> getAllPayments() throws ServiceException {
        log.debug("getAllPayments");
        try {
            List<Payment> payments = paymentDAO.findAll();
            log.debug("getAllPayments: returned {} payment(s)", payments.size());
            return payments;
        } catch (DAOException e) {
            log.error("getAllPayments DAO error: {}", e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isPaymentExists(String reservationId) throws ServiceException {
        log.debug("isPaymentExists: reservationId={}", reservationId);
        try {
            boolean exists = paymentDAO.existsForReservation(reservationId);
            log.debug("isPaymentExists: reservationId={} exists={}", reservationId, exists);
            return exists;
        } catch (DAOException e) {
            log.error("isPaymentExists DAO error for reservationId='{}': {}", reservationId, e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}


