package com.oceanview.mapper;

import com.oceanview.model.Payment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PaymentMapper implements Mapper<Payment> {
    @Override
    public Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getString("id"));
        p.setReservationId(rs.getString("reservation_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setReferenceNo(rs.getString("reference_no"));
        p.setStatus(rs.getString("status"));
        p.setProcessedBy(rs.getString("processed_by"));
        p.setNotes(rs.getString("notes"));
        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (paymentDate != null) p.setPaymentDate(paymentDate.toLocalDateTime());
        return p;
    }
}

