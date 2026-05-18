
package com.rental.rashdeen;

import com.rental.nichala.Van;
import com.rental.sandaru.PaymentStatus;
import com.rental.nichala.Vehicle;
import com.rental.nisal.VehicleService;
import com.rental.sithika.FileService;
import com.rental.nichala.Car;
import com.rental.nichala.Truck;
import com.rental.abhishek.User;
import com.rental.nichala.Bike;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private FileService fileService;

    // Create a new booking (rent a vehicle)
    public Booking bookVehicle(User user, String vehicleId, LocalDate startDate, LocalDate endDate, boolean hasDriver) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        if (!vehicle.isAvailableStatus()) {
            throw new IllegalArgumentException("Sorry, this vehicle is already rented.");
        }

        // Calculate number of rental days (minimum 1 day)
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;

        // Member 05 — Polymorphic price calculation
        // Each vehicle type (Car/Bike/Van/Truck) calculates differently
        double baseCost = vehicle.calculateRentalPrice((int) days);

        // Driver surcharge: category-based daily rate × rental duration
        // Rates: BIKE = LKR 2,500 | CAR = LKR 4,000 | VAN = LKR 5,000 | TRUCK = LKR 6,500
        double driverCharge = 0.0;
        if (hasDriver) {
            driverCharge = getDriverRatePerDay(vehicle) * days;
        }

        double totalCost = baseCost + driverCharge;

        Booking booking = new Booking(
                generateBookingId(),
                vehicleId,
                user.getId(),
                startDate,
                endDate,
                null,              // returnDate (null until returned)
                totalCost,         // totalCost = vehicle rental + driver charges
                0.0,               // lateFee (0.0 until returned)
                PaymentStatus.PENDING,
                hasDriver,
                driverCharge       // isolated driver allowance
        );

        bookingRepository.save(booking);

        // Member 01 — Mark vehicle as NOT available (it’s now rented)
        vehicleService.updateVehicleStatus(vehicleId, false);

        return booking;
    }

   
    public double getDriverRatePerDay(Vehicle vehicle) {
        if (vehicle == null) return 2500.0; // safe default
        String type = vehicle.getType() == null ? "" : vehicle.getType().toUpperCase();
        return switch (type) {
            case "BIKE"  -> 2500.0;
            case "CAR"   -> 4000.0;
            case "VAN"   -> 5000.0;
            case "TRUCK" -> 6500.0;
            default      -> 2500.0;
        };
    }

    // Process a vehicle return — calculate late fees if returned late
    public Booking returnVehicle(String bookingId, LocalDate returnDate) {
        Optional<Booking> opt = bookingRepository.findById(bookingId);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }

        Booking booking = opt.get();

        if (booking.getReturnDate() != null) {
            throw new IllegalArgumentException("This vehicle has already been returned.");
        }

        booking.setReturnDate(returnDate);

        // Member 03 — Delay charge calculation
        long lateDays = ChronoUnit.DAYS.between(booking.getEndDate(), returnDate);
        if (lateDays > 0) {
            // Get the vehicle to call its polymorphic late fee method
            Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());
            if (vehicle != null) {
                // Member 05 — calculateLateFee() is polymorphic per vehicle type
                double lateFee = vehicle.calculateLateFee((int) lateDays);
                booking.setLateFee(lateFee);
            }
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(booking); // update the booking record in file

        // Append late fee to payments.txt if applicable (Member 05)
        if (booking.getLateFee() > 0) {
            String paymentId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().toString();
            String paymentRecord = paymentId + "," + booking.getId() + "," + booking.getUserId() + "," + booking.getLateFee() + "," + timestamp;
            fileService.appendLine("payments.txt", paymentRecord);
        }

        // Member 01 — Mark vehicle as available again after return
        vehicleService.updateVehicleStatus(booking.getVehicleId(), true);

        return booking;
    }

    // Get all bookings (for Admin dashboard — Member 06)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Get bookings for a specific customer
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserId(user.getId());
    }

    // Get a single booking by ID
    public Booking getBookingById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }

   
    public void saveUpdatedBooking(Booking booking) {
        bookingRepository.save(booking);
    }

  
    private String generateBookingId() {
        java.time.LocalDate now = java.time.LocalDate.now();
        String datePart = now.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "BID" + datePart + "B";

        // Find how many bookings for today already exist
        long count = bookingRepository.findAll().stream()
                .filter(b -> b.getId().startsWith(prefix))
                .count();

        // New order number (e.g. 001, 002)
        String orderPart = String.format("%03d", count + 1);

        return prefix + orderPart;
    }

    public void cancelBooking(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        // Security: ensure the customer owns this booking
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorised to cancel this booking.");
        }
        if (booking.getReturnDate() != null) {
            throw new IllegalArgumentException("This booking has already been returned and cannot be cancelled.");
        }

        // Delete from bookings.txt
        bookingRepository.deleteById(bookingId);

        // Free up the vehicle so it can be booked again
        vehicleService.updateVehicleStatus(booking.getVehicleId(), true);
    }

    // Return booked date ranges for a specific vehicle (used for availability calendar)
    public java.util.List<String[]> getBookedDateRanges(String vehicleId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getVehicleId().equals(vehicleId) && b.getReturnDate() == null)
                .map(b -> new String[]{
                        b.getStartDate().toString(),
                        b.getEndDate().toString()
                })
                .toList();
    }
}
