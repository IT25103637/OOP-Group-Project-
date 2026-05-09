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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

// ============================================================
// HostController — Handles the "Become a Host" public form.
//
// GET  /hosts/register         → Render the registration form
// POST /hosts/submit-vehicle   → Save uploaded image to data/images/
//                                 and append a new vehicle line to vehicles.txt
//
// Images are served via WebConfig which maps /uploads/** → data/images/
// ============================================================
@Controller
@RequestMapping("/hosts")
public class HostController {

    // Runtime upload directory — must match WebConfig /uploads/** resource handler
    private static final String IMAGE_DIR = "data/images";

    @Autowired
    private VehicleService vehicleService;

    // ---- GET /hosts/register — Show the form ----
    @GetMapping("/register")
    public String showHostForm(Model model) {
        return "host/register";
    }

    // ---- POST /hosts/submit-vehicle — Process the submitted form ----
    @PostMapping("/submit-vehicle")
    public String submitVehicle(
            @RequestParam("brand")        String brand,
            @RequestParam("vehicleModel") String vehicleModel,
            @RequestParam("type")         String type,
            @RequestParam("pricePerDay")  double pricePerDay,
            @RequestParam("location")     String location,
            @RequestParam(value = "vehicleImage", required = false) MultipartFile file,
            HttpSession session,
            Model model) {

        // ---- Step 1: Handle image upload ----
        String savedImageName = "";
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            if (originalName != null && !originalName.isBlank()) {
                // Sanitize filename: lowercase + replace spaces/special chars
                savedImageName = originalName.toLowerCase()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[^a-z0-9_\\-.]", "");
            }

            if (!savedImageName.isEmpty()) {
                try {
                    // Use absolute path to guarantee correct resolution at runtime
                    Path imageDir = Paths.get(IMAGE_DIR).toAbsolutePath();
                    Files.createDirectories(imageDir); // creates all missing folders

                    Path targetPath = imageDir.resolve(savedImageName);

                    // Use Files.copy for simplicity and reliability
                    try (InputStream in = file.getInputStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    System.out.println("[HostController] Image saved to: " + targetPath);

                } catch (IOException e) {
                    System.err.println("[HostController] Image upload failed: " + e.getMessage());
                    e.printStackTrace();
                    savedImageName = ""; // clear so vehicle still saves without image
                }
            }
        }

        // ---- Step 2: Build the correct Vehicle subclass ----
        String normalizedType = (type == null) ? "CAR" : type.trim().toUpperCase();

        Vehicle vehicle = switch (normalizedType) {
            case "BIKE"  -> new Bike(null, brand, vehicleModel, pricePerDay, location, false);
            case "VAN"   -> new Van(null, brand, vehicleModel, pricePerDay, location, 0.0);
            case "TRUCK" -> new Truck(null, brand, vehicleModel, pricePerDay, location, 0);
            default      -> new Car(null, brand, vehicleModel, pricePerDay, location, 5);
        };

        // Attach the uploaded image name — stored as index 8 in vehicles.txt CSV
        if (!savedImageName.isEmpty()) {
            vehicle.setImageName(savedImageName);
        }

        // VehicleService generates VID#XXX and calls vehicleRepository.save()
        vehicleService.addVehicle(vehicle);

        // ---- Step 3: Redirect to fleet page ----
        return "redirect:/vehicles?success";
    }
}
