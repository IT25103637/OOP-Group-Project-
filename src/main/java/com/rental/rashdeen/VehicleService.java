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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ============================================================
// VehicleService — Business logic for all vehicle operations.
// Member 01: Toggle vehicle status Available <-> Rented
// Member 04: Filter/search vehicles by type, location, price
// Member 05: Price calculation is polymorphic (done in model)
// ============================================================
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Add a new vehicle — use custom professional ID format
    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId().isEmpty()) {
            vehicle.setId(generateVehicleId());
        }
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    // ID GENERATION: VID#XXX (Global Sequential Order)
    // Example: VID#001, VID#002, VID#003 ...
    private String generateVehicleId() {
        // Count ALL existing vehicles to get the next global sequence number
        long count = vehicleRepository.findAll().stream()
                .filter(v -> v.getId() != null && v.getId().startsWith("VID#"))
                .count();

        String orderPart = String.format("%03d", count + 1);
        return "VID#" + orderPart;
    }

    // Get all vehicles
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Get a single vehicle by its UUID
    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    // Delete a vehicle by ID (Admin only)
    public void deleteVehicle(String id) {
        vehicleRepository.delete(id);
    }

    // Get only vehicles that are currently available
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailable();
    }

    // Member 04 — Search with filters (type, location, price range)
    public List<Vehicle> searchVehicles(String type, String location, Double minPrice, Double maxPrice) {
        return vehicleRepository.search(type, location, minPrice, maxPrice);
    }

    // Member 01 — Set available status directly (used by BookingService)
    public void updateVehicleStatus(String id, boolean status) {
        Vehicle vehicle = getVehicleById(id);
        if (vehicle != null) {
            vehicle.setAvailableStatus(status);
            vehicleRepository.save(vehicle);
        }
    }

    // Update editable fields of an existing vehicle (Admin — Edit feature)
    // Type is NOT changed here — changing type would require swapping the subclass.
    public void updateVehicle(String id, String brand, String model,
                              double pricePerDay, String location) {
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);
        if (vehicle == null) return;
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setPricePerDay(pricePerDay);
        vehicle.setLocation(location);
        vehicleRepository.save(vehicle);
    }

    // Search with optional sort: "price_asc", "price_desc", or null (default)
    public List<Vehicle> searchVehicles(String type, String location,
                                        Double minPrice, Double maxPrice,
                                        String sort) {
        List<Vehicle> results = vehicleRepository.search(type, location, minPrice, maxPrice);
        if ("price_asc".equals(sort)) {
            results.sort(Comparator.comparingDouble(Vehicle::getPricePerDay));
        } else if ("price_desc".equals(sort)) {
            results.sort(Comparator.comparingDouble(Vehicle::getPricePerDay).reversed());
        }
        return results;
    }
}
