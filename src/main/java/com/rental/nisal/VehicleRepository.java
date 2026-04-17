/**
 * Component 02: Repositories & Data Access
 * @author Abhishek (it25102355@my.sliit.lk)
 */
package com.rental.nisal;

import com.rental.nichala.Van;
import com.rental.nichala.Vehicle;
import com.rental.sithika.FileService;
import com.rental.nichala.Car;
import com.rental.nichala.Truck;
import com.rental.nichala.Bike;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ============================================================
// VehicleRepository — File-based CRUD for vehicles.txt
// Each line is one vehicle in CSV format:
//   id,type,brand,model,pricePerDay,location,availableStatus,extraField
// ============================================================
@Repository
public class VehicleRepository {

    private static final String FILENAME = "vehicles.txt";

    @Autowired
    private FileService fileService;

    // ---- READ ALL: parse every line into the correct Vehicle subclass ----
    public List<Vehicle> findAll() {
        List<String> lines = fileService.readLines(FILENAME);
        List<Vehicle> vehicles = new ArrayList<>();

        for (String line : lines) {
            Vehicle v = parseLine(line);
            if (v != null) vehicles.add(v);
        }
        return vehicles;
    }

    // ---- READ BY ID ----
    public Optional<Vehicle> findById(String id) {
        return findAll().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
    }

    // ---- READ: only available vehicles (Member 04 — search filter) ----
    public List<Vehicle> findAvailable() {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : findAll()) {
            if (v.isAvailableStatus()) result.add(v);
        }
        return result;
    }

    // ---- SAVE (INSERT or UPDATE) ----
    public void save(Vehicle vehicle) {
        List<String> lines = fileService.readLines(FILENAME);
        String newLine = vehicle.toCsv();
        boolean found = false;

        // Check if a record with this ID already exists
        for (int i = 0; i < lines.size(); i++) {
            String existingId = lines.get(i).split(",")[0];
            if (existingId.equals(vehicle.getId())) {
                lines.set(i, newLine); // UPDATE: replace the existing line
                found = true;
                break;
            }
        }

        if (!found) {
            // INSERT: append as a new line (faster than rewriting entire file)
            fileService.appendLine(FILENAME, newLine);
        } else {
            // UPDATE: rewrite entire file with the modified list
            fileService.writeLines(FILENAME, lines);
        }
    }

    // ---- DELETE by ID ----
    public void delete(String id) {
        List<String> lines = fileService.readLines(FILENAME);
        // Keep all lines except the one with this ID
        lines.removeIf(line -> line.split(",")[0].equals(id));
        fileService.writeLines(FILENAME, lines);
    }

    // ---- TOGGLE STATUS: Available <-> Rented (Member 01) ----
    public void toggleStatus(String id) {
        Optional<Vehicle> opt = findById(id);
        opt.ifPresent(vehicle -> {
            vehicle.toggleStatus();  // flip true → false or false → true
            save(vehicle);
        });
    }

    // ---- SEARCH with filters (Member 04) ----
    public List<Vehicle> search(String type, String location, Double minPrice, Double maxPrice) {
        List<Vehicle> all = findAll();
        List<Vehicle> result = new ArrayList<>();

        // Treat slider ceiling (200 000) as "no upper limit"
        boolean noMaxFilter = (maxPrice == null || maxPrice >= 200_000.0);

        for (Vehicle v : all) {
            // Only show available vehicles in search results
            if (!v.isAvailableStatus()) continue;

            boolean typeMatch = (type == null || type.isEmpty()
                    || v.getType().equalsIgnoreCase(type));
            boolean locMatch  = (location == null || location.isEmpty()
                    || v.getLocation().toLowerCase().contains(location.toLowerCase()));
            boolean minMatch  = (minPrice == null || v.getPricePerDay() >= minPrice);
            boolean maxMatch  = noMaxFilter || v.getPricePerDay() <= maxPrice;

            if (typeMatch && locMatch && minMatch && maxMatch) {
                result.add(v);
            }
        }
        return result;
    }

    // ---- Helper: parse a single CSV line into the right Vehicle subclass ----
    private Vehicle parseLine(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            String type = parts[1]; // second field is always the type

            return switch (type.toUpperCase()) {
                case "CAR"   -> Car.fromCsv(csvLine);
                case "BIKE"  -> Bike.fromCsv(csvLine);
                case "VAN"   -> Van.fromCsv(csvLine);
                case "TRUCK" -> Truck.fromCsv(csvLine);
                default -> {
                    System.err.println("Unknown vehicle type: " + type);
                    yield null;
                }
            };
        } catch (Exception e) {
            System.err.println("Could not parse vehicle line: " + csvLine);
            return null;
        }
    }
}
