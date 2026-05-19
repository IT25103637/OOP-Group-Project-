
package com.rental.nichala;

import com.rental.nisal.WebConfig;
import com.rental.nisal.VehicleService;



import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;


@Controller
@RequestMapping("/hosts")
public class HostController {

    
    private static final String IMAGE_DIR = "data/images";

    @Autowired
    private VehicleService vehicleService;

    
    @GetMapping("/register")
    public String showHostForm(Model model) {
        return "host/register";
    }

    
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

        
        if (!savedImageName.isEmpty()) {
            vehicle.setImageName(savedImageName);
        }

        vehicleService.addVehicle(vehicle);


        return "redirect:/vehicles?success";
    }
}
