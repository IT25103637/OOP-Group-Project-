/**
 * Component 06: Reporting & System Overview
 * @author Sithika (it25103442@my.sliit.lk)
 */
package com.rental.sithika;

import com.rental.abhishek.User;
import com.rental.abhishek.Role;
import com.rental.sandaru.PaymentTransaction;



import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// PaymentReportController — Admin-only payment records viewer.
// GET /admin/payments → Shows all entries from payments.txt
// ============================================================
@Controller
@RequestMapping("/admin")
public class PaymentReportController {

    @Autowired
    private FileService fileService;

    @GetMapping("/payments")
    public String viewPayments(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        // ---- 1. Load pipe-delimited records from data/payments.txt ----
        // These come from the advanced checkout flow (PaymentTransaction objects)
        model.addAttribute("transactions", fileService.readPayments());

        // ---- 2. Load plain-text lines from src/main/resources/payments.txt ----
        // These come from the simple /process-simple-payment form
        List<String> simpleLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/payments.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) simpleLines.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Could not read src/main/resources/payments.txt: " + e.getMessage());
        }
        model.addAttribute("simpleLines", simpleLines);

        return "admin/payments";
    }

    @PostMapping("/payments/clear")
    public String clearPayments(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        fileService.clearPayments();
        return "redirect:/admin/payments?cleared";
    }
}
