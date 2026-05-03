/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.nisal;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// ============================================================
// HomeController — Handles the root "/" URL.
// After login, redirects Admin to dashboard and Customer to vehicles.
// ============================================================
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        // All users — guests, customers, and admins — see the home landing page.
        // Admins can navigate to their dashboard via the Dashboard nav link.
        return "index";
    }
}
