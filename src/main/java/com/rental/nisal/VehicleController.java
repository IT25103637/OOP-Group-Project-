/**
 * Component 04: REST & Web Controllers
 * @author Nisal (it25100031@my.sliit.lk)
 */
package com.rental.nisal;

import com.rental.nichala.Van;
import com.rental.nichala.Vehicle;
import com.rental.nichala.Car;
import com.rental.nichala.Truck;
import com.rental.abhishek.User;
import com.rental.abhishek.Role;
import com.rental.nichala.Bike;



import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ============================================================
// VehicleController — Manage vehicles (Admin CRUD + Customer browse).
// Member 01: Admin can toggle vehicle status Available <-> Rented
// Member 04: Customer can search/filter/sort vehicles
// ============================================================
@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // List ALL vehicles (Admin sees all; Customer sees available ones)
    @GetMapping
    public String listVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
        } else {
            model.addAttribute("vehicles", vehicleService.getAvailableVehicles());
        }
        return "vehicles/list";
    }

    // ---- Feature 3: Vehicle Detail Page ----
    // IMPORTANT: Spring MVC resolves literal paths (/add, /delete) BEFORE patterns (/{id}).
    // So /vehicles/add will never accidentally match this route.
    @GetMapping("/{id}")
    public String vehicleDetail(@PathVariable String id,
                                HttpSession session, Model model) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        if (vehicle == null) return "redirect:/vehicles?error";

        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("isAdmin",
                user != null && user.getRole() == Role.ADMIN);
        model.addAttribute("isCustomer",
                user != null && user.getRole() == Role.CUSTOMER);
        model.addAttribute("isGuest", user == null);
        return "vehicles/detail";
    }

    // Member 04 — Search + sort vehicles by type, location, price range, and sort order
    @GetMapping("/search")
    public String searchVehicles(@RequestParam(required = false) String type,
                                 @RequestParam(required = false) String location,
                                 @RequestParam(required = false) Double minPrice,
                                 @RequestParam(required = false) Double maxPrice,
                                 @RequestParam(defaultValue = "false") boolean hasDriver,
                                 @RequestParam(required = false) String sort,  // "price_asc" or "price_desc"
                                 Model model) {
        // ---- Feature 4: pass sort param into service ----
        List<Vehicle> results = vehicleService.searchVehicles(type, location, minPrice, maxPrice, sort);
        model.addAttribute("vehicles",  results);
        model.addAttribute("type",      type);
        model.addAttribute("location",  location);
        model.addAttribute("minPrice",  minPrice);
        model.addAttribute("maxPrice",  maxPrice);
        model.addAttribute("hasDriver", hasDriver);
        model.addAttribute("sort",      sort);
        return "vehicles/search";
    }

    // Show the Add Vehicle form (Admin only)
    @GetMapping("/add")
    public String showAddForm(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) return "redirect:/";
        return "vehicles/form";
    }

    // Process the Add Vehicle form (Admin only)
    @PostMapping("/add")
    public String addVehicle(HttpSession session,
                             @RequestParam String type,
                             @RequestParam String brand,
                             @RequestParam String model,
                             @RequestParam double pricePerDay,
                             @RequestParam String location,
                             @RequestParam(required = false, defaultValue = "0")     int    numberOfSeats,
                             @RequestParam(required = false, defaultValue = "false") boolean hasCarrier,
                             @RequestParam(required = false, defaultValue = "0.0")  double cargoCapacity,
                             @RequestParam(required = false, defaultValue = "0")    int    payloadCapacity) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) return "redirect:/";

        // ID will be generated in VehicleService using the professional format (VID...)
        Vehicle vehicle = switch (type.toUpperCase()) {
            case "CAR"   -> new Car  (null, brand, model, pricePerDay, location, numberOfSeats);
            case "BIKE"  -> new Bike (null, brand, model, pricePerDay, location, hasCarrier);
            case "VAN"   -> new Van  (null, brand, model, pricePerDay, location, cargoCapacity);
            case "TRUCK" -> new Truck(null, brand, model, pricePerDay, location, payloadCapacity);
            default      -> throw new IllegalArgumentException("Unknown type: " + type);
        };

        vehicleService.addVehicle(vehicle);
        return "redirect:/vehicles?success";
    }

    // ---- Feature 1: Show Edit Vehicle form (Admin only) ----
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id,
                               HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) return "redirect:/";

        Vehicle vehicle = vehicleService.getVehicleById(id);
        if (vehicle == null) return "redirect:/vehicles?error";

        model.addAttribute("vehicle", vehicle);
        return "vehicles/edit";
    }

    // ---- Feature 1: Process Edit Vehicle form (Admin only) ----
    // Uses a STATIC POST path — no dynamic segments in POST to avoid routing issues.
    @PostMapping("/edit")
    public String editVehicle(HttpSession session,
                              @RequestParam String vehicleId,
                              @RequestParam String brand,
                              @RequestParam String vehicleModel,
                              @RequestParam double pricePerDay,
                              @RequestParam String location) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) return "redirect:/";

        vehicleService.updateVehicle(vehicleId, brand, vehicleModel, pricePerDay, location);
        return "redirect:/vehicles?success";
    }

    // Member 01 — Toggle vehicle availability (Admin only)
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            Vehicle vehicle = vehicleService.getVehicleById(id);
            if (vehicle != null) {
                vehicleService.updateVehicleStatus(id, !vehicle.isAvailableStatus());
            }
        }
        return "redirect:/vehicles";
    }

    // Feature 2: Delete a vehicle — already existed, confirmed working
    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            vehicleService.deleteVehicle(id);
        }
        return "redirect:/vehicles?success";
    }
}
