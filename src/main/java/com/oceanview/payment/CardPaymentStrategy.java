package com.oceanview.payment;

import com.oceanview.model.Payment;
import com.oceanview.util.ServiceResult;

public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public ServiceResult execute(Payment payment) {
        String ref = payment.getReferenceNo();
        if (ref == null || ref.length() < 6) {
            return ServiceResult.failure("Card transaction ID must be at least 6 characters");
        }
        return ServiceResult.success("Card payment validated");
    }
}

