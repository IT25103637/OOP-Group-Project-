/**
 * Component 03: Business Logic & Services
 * @author Rashdeen (it25103637@my.sliit.lk)
 */
package com.rental.rashdeen;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Member 02 — Register a new user
    public User registerUser(User user) {
        // Check if email already exists (no duplicate accounts)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Determine role BEFORE ID generation (as format depends on role)
        if (userRepository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        // Give a custom ID based on role
        user.setId(generateUserId(user));

        userRepository.save(user);
        return user;
    }

    // Member 02 — Login: check email + password, return user or null
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt.get(); // login success
        }
        return null; // login failed — wrong credentials
    }

    // Get all users (Admin only — Member 06 dashboard)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by UUID
    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    // Delete user by UUID (Admin only)
    public void deleteUser(String id) {
        userRepository.delete(id);
    }

    // Update a user's name and/or password from the profile page
    public void updateUser(User updatedUser) {
        User existing = getUserById(updatedUser.getId());
        if (existing != null) {
            existing.setName(updatedUser.getName());
            // Only update password if the user typed a new one
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existing.setPassword(updatedUser.getPassword());
            }
            userRepository.save(existing);
        }
    }

    
    private String generateUserId(User user) {
        java.time.LocalDate now = java.time.LocalDate.now();

        if (user.getRole() == Role.ADMIN) {
            // UID + sanitized name + DDMMYY
            String datePart = now.format(java.time.format.DateTimeFormatter.ofPattern("ddMMyy"));
            String sanitizedName = user.getName().replaceAll("\\s+", "");
            return "UID" + sanitizedName + datePart;

        } else {
            // CID + YYMMDD + C + zero-padded global sequence number
            String datePart = now.format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));

            // Scan ALL existing users for the highest CID sequence number (e.g. 001, 002 …)
            // Pattern: CID\d{6}C(\d{3})
            int maxSeq = 0;
            for (User existing : userRepository.findAll()) {
                String eid = existing.getId();
                // CID + 6 digits + C + 3 digits = 13 chars minimum
                if (eid != null && eid.startsWith("CID") && eid.length() >= 13) {
                    try {
                        // last 3 characters are the zero-padded sequence number
                        int seq = Integer.parseInt(eid.substring(eid.length() - 3));
                        if (seq > maxSeq) maxSeq = seq;
                    } catch (NumberFormatException ignored) {
                        // non-numeric suffix — skip
                    }
                }
            }

            String orderPart = String.format("%03d", maxSeq + 1);
            return "CID" + datePart + "C" + orderPart;
        }
    }
}
