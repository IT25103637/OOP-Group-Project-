/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;


// ============================================================
// Van — Subclass of Vehicle (Polymorphism)
// Cargo premium pricing: base rate + RM50 flat fee.
// Late fee: 50% surcharge per late day.
//
// CSV extra field: cargoCapacity (kg)
// ============================================================
public class Van extends Vehicle {

    private double cargoCapacity;

    public Van() {}

    public Van(String id, String brand, String model,
               double pricePerDay, String location, double cargoCapacity) {
        super(id, "VAN", brand, model, pricePerDay, location);
        this.cargoCapacity = cargoCapacity;
    }

    public double getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(double cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    // Member 05: Premium pricing — add RM50 flat fee for cargo vans
    @Override
    public double calculateRentalPrice(int days) {
        return (getPricePerDay() * days) + 50.0;
    }

    // Member 03: Higher late fee than cars
    @Override
    public double calculateLateFee(int lateDays) {
        return getPricePerDay() * 1.5 * lateDays;
    }

    @Override
    public String getVehicleDescription() {
        return "Van: " + getBrand() + " " + getModel() + " (Capacity: " + cargoCapacity + " kg)";
    }

    @Override
    public String toCsv() {
        return baseCsv() + "," + cargoCapacity + imageNameCsv();
    }

    public static Van fromCsv(String csvLine) {
        // Format: id,VAN,brand,model,pricePerDay,location,availableStatus,cargoCapacity[,imageName]
        String[] p = csvLine.split(",");
        Van van = new Van(p[0], p[2], p[3], Double.parseDouble(p[4]), p[5], Double.parseDouble(p[7]));
        van.setAvailableStatus(Boolean.parseBoolean(p[6]));
        if (p.length > 8 && p[8] != null && !p[8].trim().isEmpty()) {
            van.setImageName(p[8].trim());
        }
        return van;
    }
}
