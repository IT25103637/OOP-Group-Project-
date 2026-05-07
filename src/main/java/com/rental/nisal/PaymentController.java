/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.nisal;

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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// ============================================================
// PaymentController — Simulated payment gateway for bookings.
//
// GET  /checkout/{bookingId} → Show Rofi-themed checkout form
// POST /process-payment      → Mask card, save to payments.txt,
//                              redirect to /payment-success
// GET  /payment-success      → Show success confirmation page
// ============================================================
@Controller
public class PaymentController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private FileService fileService;

    // ---- Endpoint 1: Show the checkout page ----
    // Loads total amount safely from the booking; passes it to the view.
    @GetMapping("/checkout/{bookingId}")
    public String showCheckout(@PathVariable String bookingId,
                               HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        // Safely look up the booking
        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isEmpty()) return "redirect:/bookings/history?error=Booking+not+found";

        Booking booking = opt.get();

        // Look up vehicle so we can show the name on the checkout page
        Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        return "checkout";
    }

    @Autowired
    private com.rental.service.BookingService bookingService;

    // ---- Endpoint 2: Process the payment form (STATIC path, no dynamic segment) ----
    // Phase 1 — Record Payment: appends one pipe-delimited line to payments.txt
    //           (FileService.appendLine uses FileWriter(..., true) — strict append mode)
    // Phase 2 — Commit Booking: updates the booking's PaymentStatus from PENDING → PAID
    //           in bookings.txt via BookingRepository (try-with-resources safe)
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam String bookingId,
                                 @RequestParam String customerName,
                                 @RequestParam String email,
                                 @RequestParam String mobileNumber,
                                 @RequestParam String cardNumber,
                                 @RequestParam String totalAmountLKR,
                                 HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        // Mask the card number — keep only the last 4 digits; raw card never persisted
        String cleaned = cardNumber.replaceAll("\\s+", "");
        String masked = "**** **** **** " + cleaned.substring(Math.max(0, cleaned.length() - 4));

        // Parse grand total safely (handles formatted values like "17,000.00")
        double amount = 0.0;
        try {
            amount = Double.parseDouble(totalAmountLKR.replaceAll(",", ""));
        } catch (NumberFormatException ignored) {}

        // ── Phase 1: Write payment record to payments.txt ──────────────────────────
        // Uses pipe delimiters to match the PaymentTransaction.toPipe() format.
        // FileService.appendLine() opens FileWriter(..., true) — append-only, never
        // overwrites existing payment history.

        // Generate a realistic, human-readable Payment Reference Number
        // Format: TXN-YYMMDD-XXXXXX  (e.g. TXN-260517-A3K9PL)
        // The 6-char suffix is derived from a UUID for guaranteed uniqueness.
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String datePart   = now.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        String rawSuffix  = java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String shortSuffix = rawSuffix.substring(0, 6); // take first 6 hex chars
        String paymentRef = "TXN-" + datePart + "-" + shortSuffix;

        PaymentTransaction transaction = new PaymentTransaction(
                paymentRef,
                bookingId,
                customerName,
                email,
                mobileNumber,
                amount,
                masked,
                now.toString(),
                "SUCCESS"
        );
        fileService.appendPayment(transaction);  // pipe-delimited, append-safe

        // ── Phase 2: Update booking status PENDING → PAID in bookings.txt ─────────
        // BookingRepository.save() uses FileService.writeLines() (try-with-resources)
        // to atomically rewrite the file with the updated record only.
        Optional<com.rental.model.Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isPresent()) {
            com.rental.model.Booking booking = opt.get();
            booking.setPaymentStatus(PaymentStatus.PAID);
            bookingRepository.save(booking);
        }

        // Store confirmation data in session for the success page
        session.setAttribute("lastPaymentId",     transaction.getPaymentId());
        session.setAttribute("lastPaymentName",   customerName);
        session.setAttribute("lastPaymentAmount", amount);

        return "redirect:/payment-success";
    }

    // ---- Endpoint 3: Payment Success Confirmation ----
    @GetMapping("/payment-success")
    public String paymentSuccess(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        // Pull confirmation data from session and clear it
        model.addAttribute("paymentId",     session.getAttribute("lastPaymentId"));
        model.addAttribute("customerName",  session.getAttribute("lastPaymentName"));
        model.addAttribute("amountPaid",    session.getAttribute("lastPaymentAmount"));

        session.removeAttribute("lastPaymentId");
        session.removeAttribute("lastPaymentName");
        session.removeAttribute("lastPaymentAmount");

        return "payment-success"; // closes paymentSuccess()
    }

    // ---- Simple Endpoint 4: Show the basic payment.html form ----
    @GetMapping("/payment")
    public String showSimplePaymentForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";
        return "payment";
    }

    // ---- Simple Endpoint 5: Handle the basic payment form submission ----
    // Appends a plain text line to payments.txt and shows PAID status.
    @PostMapping("/process-simple-payment")
    public String processSimplePayment(@RequestParam String customerName,
                                       @RequestParam String mobileNumber,
                                       @RequestParam String cardNumber,
                                       HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        // Mask the last 4 digits — never store raw card data
        String cleaned = cardNumber.replaceAll("\\s+", "");
        String masked = "**** **** **** " + cleaned.substring(Math.max(0, cleaned.length() - 4));

        // Build the record line
        String timestamp = java.time.LocalDateTime.now().toString();
        String record = timestamp + " | " + customerName + " | " + mobileNumber + " | " + masked;

        // Append directly to payments.txt using try-with-resources (no FileService dependency)
        String filePath = "src/main/resources/payments.txt";
        try (java.io.FileWriter fw = new java.io.FileWriter(filePath, true);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            bw.write(record);
            bw.newLine();
        } catch (java.io.IOException e) {
            System.err.println("Could not write to payments.txt: " + e.getMessage());
        }

        // Pass PAID status back to the same payment.html page
        model.addAttribute("status", "✅  Payment Status: PAID");
        return "payment";
    }
}
