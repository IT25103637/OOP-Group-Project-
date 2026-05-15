/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.abhishek;


import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Show the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        // Basic required field check
        if (user.getName() == null || user.getName().isEmpty() ||
            user.getEmail() == null || user.getEmail().isEmpty() ||
            user.getPassword() == null || user.getPassword().length() < 6) {
            model.addAttribute("error", "Please fill all fields. Password must be at least 6 characters.");
            return "users/register";
        }

        try {
            userService.registerUser(user);
            return "redirect:/users/login?success"; // go to login with success message
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "users/register";
        }
    }

    // Show the login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "users/login";
    }

    // Member 02 — Process login and redirect based on role
    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        User user = userService.loginUser(email, password);

        if (user != null) {
            session.setAttribute("loggedInUser", user); // save user in session

            // Member 02 — Redirect Admin to admin dashboard, Customer to vehicles
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/vehicles";
            }
        } else {
            model.addAttribute("error", "Invalid email or password. Please try again.");
            return "users/login";
        }
    }

    // Logout — clear the session
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // destroy all session data
        return "redirect:/users/login";
    }

    // Show the profile page
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/users/login";

        model.addAttribute("user", userService.getUserById(loggedInUser.getId()));
        return "users/profile";
    }

    // Process profile update (name and/or password)
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/users/login";

        updatedUser.setId(loggedInUser.getId());   // ensure we update the right record
        updatedUser.setRole(loggedInUser.getRole()); // preserve role from session
        userService.updateUser(updatedUser);

        // Refresh session with updated info
        session.setAttribute("loggedInUser", userService.getUserById(updatedUser.getId()));
        return "redirect:/users/profile?success";
    }
}
