package com.rental.rashdeen;

import com.rental.sandaru.PaymentStatus;



import java.time.LocalDate;

public class Booking {

    private String id;
    private String vehicleId;
    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;  // null until the vehicle is returned
    private double totalCost;      // vehicle rental charges + driver charges
    private double lateFee;
    private PaymentStatus paymentStatus;
    private boolean hasDriver;     // true when a professional driver is requested
    private double driverCharge;   // driver allowance (category-based rate × days); 0 if no driver

    // Default constructor
    public Booking() {}

    // Full constructor used when creating or updating a booking
    public Booking(String id, String vehicleId, String userId,
                   LocalDate startDate, LocalDate endDate, LocalDate returnDate,
                   double totalCost, double lateFee, PaymentStatus paymentStatus,
                   boolean hasDriver, double driverCharge) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.returnDate = returnDate;
        this.totalCost = totalCost;
        this.lateFee = lateFee;
        this.paymentStatus = paymentStatus;
        this.hasDriver = hasDriver;
        this.driverCharge = driverCharge;
    }

    // Shorter constructor for new bookings (legacy support)
    public Booking(String id, String vehicleId, String userId,
                   LocalDate startDate, LocalDate endDate, double totalCost,
                   boolean hasDriver, double driverCharge) {
        this(id, vehicleId, userId, startDate, endDate, null, totalCost, 0.0, PaymentStatus.PENDING,
                hasDriver, driverCharge);
    }

    
    public String toCsv() {
        String returnDateStr = (returnDate != null) ? returnDate.toString() : "NULL";
        return id + "," + vehicleId + "," + userId + "," + startDate + ","
                + endDate + "," + returnDateStr + "," + totalCost + ","
                + lateFee + "," + paymentStatus.name() + "," + hasDriver + "," + driverCharge;
    }

    public static Booking fromCsv(String csvLine) {
        String[] p = csvLine.split(",");
        if (p.length < 9) return null; // skip bad/incomplete lines

        Booking b = new Booking();
        b.id            = p[0];
        b.vehicleId     = p[1];
        b.userId        = p[2];
        b.startDate     = LocalDate.parse(p[3]);
        b.endDate       = LocalDate.parse(p[4]);
        b.returnDate    = p[5].equals("NULL") ? null : LocalDate.parse(p[5]);
        b.totalCost     = Double.parseDouble(p[6]);
        b.lateFee       = Double.parseDouble(p[7]);
        b.paymentStatus = PaymentStatus.valueOf(p[8]);

        
        b.hasDriver = (p.length >= 10) && Boolean.parseBoolean(p[9]);

    
        b.driverCharge = (p.length >= 11) ? Double.parseDouble(p[10]) : 0.0;

        return b;
    }

    // Getters & Setters 

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public boolean isHasDriver() { return hasDriver; }
    public void setHasDriver(boolean hasDriver) { this.hasDriver = hasDriver; }

    public double getDriverCharge() { return driverCharge; }
    public void setDriverCharge(double driverCharge) { this.driverCharge = driverCharge; }

    public double getGrandTotal() { return totalCost + lateFee; }
}
