/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.rashdeen;

import com.rental.sandaru.PaymentStatus;
import com.rental.nichala.Vehicle;
import com.rental.sandaru.LuxuryVehicleFee;
import com.rental.nisal.VehicleService;
import com.rental.abhishek.User;
import com.rental.abhishek.Role;



import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    // Injected for the polymorphic late fee selection (Component 03)
    @Autowired
    private LateFeeFactory lateFeeFactory;

    
    @GetMapping("/new/{vehicleId}")
    public String showBookingForm(@PathVariable String vehicleId,
                                  @RequestParam(defaultValue = "false") boolean hasDriver,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        if (vehicle == null || !vehicle.isAvailableStatus()) {
            return "redirect:/vehicles?unavailable";
        }

        // Build a JSON array of booked date ranges for Flatpickr to disable
        java.util.List<String[]> ranges = bookingService.getBookedDateRanges(vehicleId);
        StringBuilder jsonRanges = new StringBuilder("[");
        for (int i = 0; i < ranges.size(); i++) {
            jsonRanges.append("{\"from\":\"").append(ranges.get(i)[0])
                      .append("\",\"to\":\"").append(ranges.get(i)[1]).append("\"}");
            if (i < ranges.size() - 1) jsonRanges.append(",");
        }
        jsonRanges.append("]");

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("hasDriver", hasDriver);
        model.addAttribute("bookedRanges", jsonRanges.toString());
        return "bookings/new";
    }

    
    @PostMapping("/new")
    public String createBooking(@RequestParam String vehicleId,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                @RequestParam(defaultValue = "false") boolean hasDriver,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        try {
            bookingService.bookVehicle(user, vehicleId, startDate, endDate, hasDriver);
            return "redirect:/bookings/history?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehicle", vehicleService.getVehicleById(vehicleId));
            model.addAttribute("hasDriver", hasDriver);
            return "bookings/new";
        }
    }

    
    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        List<Booking> bookings;
        if (user.getRole() == Role.ADMIN) {
            bookings = bookingService.getAllBookings();
        } else {
            bookings = bookingService.getUserBookings(user);
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("vehicleService", vehicleService);
        return "bookings/history";
    }

    
    @GetMapping("/list")
    public String viewBookingList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        // Only Admin may access the full management list
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("vehicleService", vehicleService);
        return "bookings/list";
    }

  
    @GetMapping("/return")
    public String showReturnForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }
        List<Booking> activeBookings = bookingService.getAllBookings()
                .stream()
                .filter(b -> b.getReturnDate() == null)
                .toList();
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("vehicleService", vehicleService);
        return "bookings/return";
    }

    
    @GetMapping("/return/{id}")
    public String showReturnFormForBooking(@PathVariable("id") String bookingId,
                                           HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }
        List<Booking> activeBookings = bookingService.getAllBookings()
                .stream()
                .filter(b -> b.getReturnDate() == null)
                .toList();
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("vehicleService", vehicleService);
        // Pre-select the booking that was clicked
        model.addAttribute("selectedBookingId", bookingId);
        return "bookings/return";
    }

    
    @PostMapping("/process-return")
    public String processReturn(
            @RequestParam("bookingId") String bookingId,
            @RequestParam("actualReturnDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate actualReturnDate,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        // --- Step 1: Retrieve the booking ---
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            model.addAttribute("errorMsg", "Booking ID \"" + bookingId + "\" was not found in bookings.txt.");
            model.addAttribute("activeBookings", bookingService.getAllBookings()
                    .stream().filter(b -> b.getReturnDate() == null).toList());
            model.addAttribute("vehicleService", vehicleService);
            return "bookings/return";
        }

        if (booking.getReturnDate() != null) {
            model.addAttribute("errorMsg", "This booking has already been marked as returned on "
                    + booking.getReturnDate() + ".");
            model.addAttribute("activeBookings", bookingService.getAllBookings()
                    .stream().filter(b -> b.getReturnDate() == null).toList());
            model.addAttribute("vehicleService", vehicleService);
            return "bookings/return";
        }

        // --- Step 2: Calculate days late ---
        long daysLate = ChronoUnit.DAYS.between(booking.getEndDate(), actualReturnDate);
        if (daysLate < 0) daysLate = 0; // returned early — no penalty

        // --- Step 3: Retrieve the vehicle for brand and price info ---
        Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());
        double pricePerDay = (vehicle != null) ? vehicle.getPricePerDay() : 5000.0;
        String brand = (vehicle != null) ? vehicle.getBrand() : "";
        String vehicleLabel = (vehicle != null)
                ? vehicle.getBrand() + " " + vehicle.getModel()
                : "Vehicle ID: " + booking.getVehicleId();

        
        LateFeeCalculator calculator = lateFeeFactory.getCalculator(brand);
        String feeType = (calculator instanceof LuxuryVehicleFee) ? "Luxury" : "Standard";
        double lateFee = calculator.calculateFee(daysLate, pricePerDay);

        
        booking.setReturnDate(actualReturnDate);
        booking.setLateFee(lateFee);
        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingService.saveUpdatedBooking(booking);

        //Free the vehicle so it can be booked again
        if (vehicle != null) {
            vehicleService.updateVehicleStatus(booking.getVehicleId(), true);
        }

        // Step 8: Pass results to the template
        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleLabel", vehicleLabel);
        model.addAttribute("daysLate", daysLate);
        model.addAttribute("lateFee", lateFee);
        model.addAttribute("feeType", feeType);
        model.addAttribute("successReturn", true);

        return "bookings/return";
    }

    // ── GET: Show the dedicated Extend Booking form page ─────────────────────
    @GetMapping("/extend/{id}")
    public String showExtendForm(@PathVariable("id") String bookingId,
                                 HttpSession session,
                                 Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            return "redirect:/bookings/history?extendError=BookingNotFound";
        }

        // Ownership check for customers
        if (user.getRole() != Role.ADMIN && !booking.getUserId().equals(user.getId())) {
            return "redirect:/bookings/history?extendError=Unauthorized";
        }

        // Cannot extend a returned booking
        if (booking.getReturnDate() != null) {
            return "redirect:/bookings/history?extendError=AlreadyReturned";
        }

        Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        return "bookings/extend";
    }

    // ── POST: Process the extension and persist to bookings.txt ──────────────

    @PostMapping("/extend")
    public String extendBooking(
            @RequestParam("bookingId") String bookingId,
            @RequestParam("newEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newEndDate,
            HttpSession session,
            Model model) {

        // --- Guard: must be authenticated ---
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/users/login";
        }

        // --- Step 1: Retrieve the booking ---
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/bookings/list?extendError=BookingNotFound";
            }
            return "redirect:/bookings/history?extendError=BookingNotFound";
        }

        // --- Step 2: Ownership Validation Safeguard ---
        // Customers may only extend their own bookings
        if (user.getRole() != Role.ADMIN) {
            if (!booking.getUserId().equals(user.getId())) {
                return "redirect:/bookings/history?extendError=Unauthorized";
            }
        }

        // --- Step 3: Validate the new date ---
        if (!newEndDate.isAfter(booking.getEndDate())) {
            // New date is not after current end date — reject
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/bookings/list?extendError=InvalidDate";
            }
            return "redirect:/bookings/history?extendError=InvalidDate";
        }

        if (booking.getReturnDate() != null) {
            // Cannot extend an already-returned booking
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/bookings/list?extendError=AlreadyReturned";
            }
            return "redirect:/bookings/history?extendError=AlreadyReturned";
        }

        // --- Step 4: Recalculate total cost for the extended duration ---
        Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());
        if (vehicle != null) {
            long totalDays = ChronoUnit.DAYS.between(booking.getStartDate(), newEndDate);
            if (totalDays <= 0) totalDays = 1;
            double baseCost = vehicle.calculateRentalPrice((int) totalDays);
            // Re-compute driver charge using the correct category-based rate
            double driverCharge = 0.0;
            if (booking.isHasDriver()) {
                driverCharge = bookingService.getDriverRatePerDay(vehicle) * totalDays;
            }
            booking.setTotalCost(baseCost + driverCharge);
            booking.setDriverCharge(driverCharge);
        }

        // --- Step 5: Update the end date and persist ---
        booking.setEndDate(newEndDate);
        bookingService.saveUpdatedBooking(booking);

        // --- Step 6: Dynamic redirect based on role ---
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/bookings/list?extended=true";
        }
        return "redirect:/bookings/history?extended=true";
    }

    
    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        try {
            bookingService.cancelBooking(id, user.getId());
            return "redirect:/bookings/history?cancelled";
        } catch (IllegalArgumentException e) {
            return "redirect:/bookings/history?error=" + e.getMessage();
        }
    }
}
