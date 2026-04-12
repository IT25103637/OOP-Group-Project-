/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;


// ============================================================
// Vehicle — Abstract base class (Polymorphism demonstrated)
// Car, Bike, Van, Truck all extend this class.
// Each subclass overrides: calculateRentalPrice(), calculateLateFee()
//
// CSV format in vehicles.txt:
//   id,type,brand,model,pricePerDay,location,availableStatus,extraField
// ============================================================
public abstract class Vehicle {

    private String id;           // UUID string
    private String type;         // CAR, BIKE, VAN, TRUCK
    private String brand;        // e.g. "Toyota"
    private String model;        // e.g. "Corolla"
    private double pricePerDay;
    private String location;
    private boolean availableStatus;
    private String imageName;    // Optional: custom uploaded image filename (index 8 in CSV)

    // Default constructor
    public Vehicle() {}

    // Common constructor for all subclasses
    public Vehicle(String id, String type, String brand, String model,
                   double pricePerDay, String location) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.pricePerDay = pricePerDay;
        this.location = location;
        this.availableStatus = true; // new vehicles start as available
    }

    // ---- Polymorphic abstract methods ----
    // Each vehicle type calculates price and late fees differently (Member 05)
    public abstract double calculateRentalPrice(int days);
    public abstract double calculateLateFee(int lateDays);
    public abstract String getVehicleDescription();

    // ---- CSV helpers (implemented by subclasses) ----
    // Subclasses add their own extra field at the end
    public abstract String toCsv();

    // The common part of the CSV (shared by all vehicle types)
    // imageName is appended as the 9th field — null-safe (writes empty string if absent)
    protected String baseCsv() {
        return id + "," + type + "," + brand + "," + model + ","
                + pricePerDay + "," + location + "," + availableStatus;
    }

    // The imageName suffix appended by subclasses that support custom images
    protected String imageNameCsv() {
        return (imageName != null && !imageName.isEmpty()) ? "," + imageName : ",";
    }

    // Toggle availability: Available <-> Rented (Member 01)
    public void toggleStatus() {
        this.availableStatus = !this.availableStatus;
    }

    // ---------- Getters & Setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isAvailableStatus() { return availableStatus; }
    public void setAvailableStatus(boolean availableStatus) { this.availableStatus = availableStatus; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
}
