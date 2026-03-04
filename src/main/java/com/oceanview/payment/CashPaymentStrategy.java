package com.oceanview.payment;

import com.oceanview.model.Payment;
import com.oceanview.util.ServiceResult;

public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public ServiceResult execute(Payment payment) {
        String ref = payment.getReferenceNo();
        if (ref == null || ref.length() < 3) {
            return ServiceResult.failure("Cash receipt number must be at least 3 characters");
        }
        return ServiceResult.success("Cash payment validated");
    }
}

