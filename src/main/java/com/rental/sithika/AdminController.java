/**
 * Component 06: Reporting & System Overview
 * @author Sithika (it25103442@my.sliit.lk)
 */
package com.rental.sithika;

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

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    // Member 06 â€” Admin dashboard with full platform statistics
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }

        // Load stats from the service (reads from files)
        model.addAttribute("stats", dashboardService.getDashboardStats());
        
        // Load payment transactions for the payments report table
        model.addAttribute("payments", fileService.readPayments());
        
        return "admin/dashboard";
    }

    // List all registered users (Admin only)
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/users/login";
        }
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // Delete a user by their UUID (Admin only)
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users";
    }
}

