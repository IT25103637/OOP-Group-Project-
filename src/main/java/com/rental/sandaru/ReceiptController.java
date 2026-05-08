/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.sandaru;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// ============================================================
// ReceiptController — Shows a printable booking receipt page.
// GET /bookings/receipt/{bookingId} → renders receipt.html
// The user clicks "Download / Print" which calls window.print()
// No external library needed — uses browser PDF printing.
// ============================================================
@Controller
public class ReceiptController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/bookings/receipt/{bookingId}")
    public String showReceipt(@PathVariable String bookingId,
                              HttpSession session,
                              Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) return "redirect:/bookings/history?error=Booking+not+found";

        // Security: only the booking owner or admin can view
        if (!booking.getUserId().equals(user.getId())
                && user.getRole() != Role.ADMIN) {
            return "redirect:/bookings/history?error=Access+denied";
        }

        Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("user", user);
        // Driver rate for this vehicle type (shown in cost breakdown)
        double driverRatePerDay = (vehicle != null)
                ? bookingService.getDriverRatePerDay(vehicle) : 2500.0;
        model.addAttribute("driverRatePerDay", driverRatePerDay);

        return "bookings/receipt";
    }
}
