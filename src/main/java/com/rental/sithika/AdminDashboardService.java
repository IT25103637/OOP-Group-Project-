/**
 * Component 06: Reporting & System Overview
 * @author Sithika (it25103442@my.sliit.lk)
 */
package com.rental.sithika;

import com.rental.rashdeen.Booking;
import com.rental.nichala.Vehicle;
import com.rental.rashdeen.BookingService;
import com.rental.nisal.VehicleService;
import com.rental.abhishek.UserService;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// ============================================================
// AdminDashboardService — Summarises platform stats for Admin.
// Member 06: Total revenue and total bookings from files.
// ============================================================
@Service
public class AdminDashboardService {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    // Build the stats map that the Admin Dashboard page displays
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Vehicle> allVehicles  = vehicleService.getAllVehicles();
        List<Booking> allBookings  = bookingService.getAllBookings();

        // Count totals
        long totalVehicles    = allVehicles.size();
        long availableVehicles = allVehicles.stream().filter(Vehicle::isAvailableStatus).count();
        long activeBookings   = allBookings.stream().filter(b -> b.getReturnDate() == null).count();
        long totalUsers       = userService.getAllUsers().size();

        // Member 06 — Total revenue = all rental costs + all late fees
        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalCost() + b.getLateFee())
                .sum();

        // Find which vehicle type is rented the most
        String mostRentedType = "N/A";
        if (!allBookings.isEmpty()) {
            Map<String, Long> typeCounts = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getVehicleId, Collectors.counting()));

            // Count by vehicle type — map vehicleId to type first
            Map<String, Long> typeCountByType = new HashMap<>();
            for (Booking b : allBookings) {
                Vehicle v = vehicleService.getVehicleById(b.getVehicleId());
                if (v != null) {
                    String type = v.getType();
                    typeCountByType.put(type, typeCountByType.getOrDefault(type, 0L) + 1);
                }
            }

            mostRentedType = typeCountByType.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        }

        // ---- Monthly Revenue for Chart.js (last 6 months) ----
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        // Collect all months present in bookings, sorted
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        allBookings.stream()
                .filter(b -> b.getStartDate() != null)
                .sorted(Comparator.comparing(Booking::getStartDate))
                .forEach(b -> {
                    String month = b.getStartDate().format(monthFmt);
                    revenueByMonth.merge(month, b.getTotalCost() + b.getLateFee(), Double::sum);
                });

        List<String> monthLabels  = new ArrayList<>(revenueByMonth.keySet());
        List<Double> monthRevenue = new ArrayList<>(revenueByMonth.values());

        // Put all data into the stats map for Thymeleaf to display
        stats.put("totalVehicles",     totalVehicles);
        stats.put("availableVehicles", availableVehicles);
        stats.put("activeBookings",    activeBookings);
        stats.put("totalUsers",        totalUsers);
        stats.put("totalRevenue",      totalRevenue);
        stats.put("mostRentedType",    mostRentedType);
        stats.put("totalBookings",     allBookings.size());
        stats.put("monthLabels",       monthLabels);
        stats.put("monthRevenue",      monthRevenue);

        return stats;
    }
}
