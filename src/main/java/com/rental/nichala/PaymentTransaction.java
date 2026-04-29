/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

// ============================================================
// PaymentTransaction — Stores a single simulated payment record.
// Card data is NEVER stored raw; only the masked version is saved.
//
// Pipe-delimited format in payments.txt:
//   paymentId|bookingId|customerName|email|mobile|totalAmountLKR|maskedCard|timestamp|status
// ============================================================
public class PaymentTransaction {

    private String paymentId;       // UUID
    private String bookingId;       // References the Booking
    private String customerName;    // Full name from checkout form
    private String email;           // Customer email
    private String mobileNumber;    // Customer mobile
    private double totalAmountLKR;  // Final amount charged
    private String maskedCardNumber; // e.g. "**** **** **** 1234"
    private String timestamp;       // ISO datetime string
    private String status;          // SUCCESS / FAILED

    // Default constructor
    public PaymentTransaction() {}

    // Full constructor used when creating a new transaction
    public PaymentTransaction(String paymentId, String bookingId, String customerName,
                               String email, String mobileNumber, double totalAmountLKR,
                               String maskedCardNumber, String timestamp, String status) {
        this.paymentId       = paymentId;
        this.bookingId       = bookingId;
        this.customerName    = customerName;
        this.email           = email;
        this.mobileNumber    = mobileNumber;
        this.totalAmountLKR  = totalAmountLKR;
        this.maskedCardNumber = maskedCardNumber;
        this.timestamp       = timestamp;
        this.status          = status;
    }

    // ---- Pipe-delimited Serialization ----

    // Convert to one pipe-delimited line for payments.txt
    public String toPipe() {
        return paymentId + "|" + bookingId + "|" + customerName + "|"
                + email + "|" + mobileNumber + "|" + totalAmountLKR + "|"
                + maskedCardNumber + "|" + timestamp + "|" + status;
    }

    // Build a PaymentTransaction from a pipe-delimited line
    public static PaymentTransaction fromPipe(String pipeLine) {
        String[] p = pipeLine.split("\\|");
        if (p.length < 9) return null; // skip bad/incomplete lines

        PaymentTransaction t = new PaymentTransaction();
        t.paymentId        = p[0];
        t.bookingId        = p[1];
        t.customerName     = p[2];
        t.email            = p[3];
        t.mobileNumber     = p[4];
        t.totalAmountLKR   = Double.parseDouble(p[5]);
        t.maskedCardNumber = p[6];
        t.timestamp        = p[7];
        t.status           = p[8];
        return t;
    }

    // ---------- Getters & Setters ----------

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public double getTotalAmountLKR() { return totalAmountLKR; }
    public void setTotalAmountLKR(double totalAmountLKR) { this.totalAmountLKR = totalAmountLKR; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
