package com.oceanview.payment;

import com.oceanview.model.Payment;
import com.oceanview.util.ServiceResult;

public interface PaymentStrategy {
    ServiceResult execute(Payment payment);
}

