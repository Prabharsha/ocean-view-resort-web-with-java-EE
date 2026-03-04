package com.oceanview.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Payment implements Serializable {
    private String id;
    private String reservationId;
    private double amount;
    private String paymentMethod;   // "CASH" or "CARD"
    private String referenceNo;     // CASH: cash receipt number, CARD: card terminal transaction ID
    private String status;          // PENDING | COMPLETED | FAILED | REFUNDED
    private String processedBy;     // FK to users.id
    private String notes;
    private LocalDateTime paymentDate;

    public Payment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    @Override
    public String toString() {
        return "Payment{id='" + id + "', reservationId='" + reservationId + "', amount=" + amount
            + ", method='" + paymentMethod + "', status='" + status + "'}";
    }
}

