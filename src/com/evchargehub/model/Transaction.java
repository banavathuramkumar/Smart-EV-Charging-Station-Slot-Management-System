package com.evchargehub.model;

/**
 * Represents a simulated financial transaction for a completed EV charging session.
 */
public class Transaction {
    private String transactionId;
    private int sessionId;
    private int userId;
    private double amount;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String timestamp;

    public Transaction() {
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public Transaction(String transactionId, int sessionId, int userId, double amount, String paymentMethod, PaymentStatus paymentStatus, String timestamp) {
        this.transactionId = transactionId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return transactionId + " - $" + String.format("%.2f", amount) + " (" + paymentStatus + ")";
    }
}
