package com.oceanview.payment;

import com.oceanview.model.Payment;
import com.oceanview.util.ServiceResult;

public class PaymentContext {
    private PaymentStrategy strategy;

    public void setStrategy(PaymentStrategy s) {
        this.strategy = s;
    }

    public ServiceResult executePayment(Payment p) {
        return strategy.execute(p);
    }
}

