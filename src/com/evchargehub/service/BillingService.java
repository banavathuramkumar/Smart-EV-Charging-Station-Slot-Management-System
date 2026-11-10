package com.evchargehub.service;

import com.evchargehub.dao.TransactionDAO;
import com.evchargehub.dao.UserDAO;
import com.evchargehub.exceptions.EVChargeHubException;
import com.evchargehub.model.PaymentStatus;
import com.evchargehub.model.Person;
import com.evchargehub.model.Transaction;
import com.evchargehub.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service managing billing, simulated payments, wallet deductions, and transactions.
 */
public class BillingService {

    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;

    public BillingService() {
        this.transactionDAO = new TransactionDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Processes simulated payment for a completed charging session.
     */
    public Transaction processPayment(int sessionId, int userId, double amount, String paymentMethod) throws EVChargeHubException, SQLException {
        if (amount < 0.0) {
            throw new EVChargeHubException("Invalid billing amount: $" + amount);
        }

        // Generate unique transaction identifier
        String timestampCode = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String shortUuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String txnId = "TXN-" + timestampCode + "-" + shortUuid;

        PaymentStatus status = PaymentStatus.PAID;

        // If paying via wallet, verify and deduct balance
        if ("WALLET".equalsIgnoreCase(paymentMethod)) {
            Person person = userDAO.findById(userId);
            if (person instanceof User u) {
                if (u.getWalletBalance() < amount) {
                    throw new EVChargeHubException("Insufficient wallet balance ($" + String.format("%.2f", u.getWalletBalance()) + "). Required: $" + String.format("%.2f", amount));
                }
                double newBalance = u.getWalletBalance() - amount;
                userDAO.updateWalletBalance(userId, newBalance);
                u.setWalletBalance(newBalance);
            }
        }

        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Transaction txn = new Transaction(txnId, sessionId, userId, amount, paymentMethod, status, formattedTime);

        boolean ok = transactionDAO.insert(txn);
        if (!ok) {
            throw new EVChargeHubException("Failed to record payment transaction in database.");
        }

        System.out.println("[BillingService] Payment successful: " + txn);
        return txn;
    }

    public List<Transaction> getUserTransactions(int userId) throws SQLException {
        return transactionDAO.findByUserId(userId);
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public double getTotalRevenue() throws SQLException {
        return transactionDAO.calculateTotalRevenue();
    }
}
