/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;


// ============================================================
// Bike — Subclass of Vehicle (Polymorphism)
// Economy pricing: 10% discount if rented more than 3 days.
// Late fee: 10% surcharge per late day (lowest penalty).
//
// CSV extra field: hasCarrier (true/false)
// ============================================================
public class Bike extends Vehicle {

    private boolean hasCarrier;

    public Bike() {}

    public Bike(String id, String brand, String model,
                double pricePerDay, String location, boolean hasCarrier) {
        super(id, "BIKE", brand, model, pricePerDay, location);
        this.hasCarrier = hasCarrier;
    }

    public boolean isHasCarrier() { return hasCarrier; }
    public void setHasCarrier(boolean hasCarrier) { this.hasCarrier = hasCarrier; }

    // Member 05: 10% discount for rentals longer than 3 days
    @Override
    public double calculateRentalPrice(int days) {
        double total = getPricePerDay() * days;
        if (days > 3) {
            total = total * 0.9; // 10% discount
        }
        return total;
    }

    // Member 03: Bikes have the lowest late fee
    @Override
    public double calculateLateFee(int lateDays) {
        return getPricePerDay() * 1.1 * lateDays;
    }

    @Override
    public String getVehicleDescription() {
        return "Bike: " + getBrand() + " " + getModel()
                + (hasCarrier ? " with Carrier" : " without Carrier");
    }

    @Override
    public String toCsv() {
        return baseCsv() + "," + hasCarrier + imageNameCsv();
    }

    public static Bike fromCsv(String csvLine) {
        // Format: id,BIKE,brand,model,pricePerDay,location,availableStatus,hasCarrier[,imageName]
        String[] p = csvLine.split(",");
        Bike bike = new Bike(p[0], p[2], p[3], Double.parseDouble(p[4]), p[5], Boolean.parseBoolean(p[7]));
        bike.setAvailableStatus(Boolean.parseBoolean(p[6]));
        if (p.length > 8 && p[8] != null && !p[8].trim().isEmpty()) {
            bike.setImageName(p[8].trim());
        }
        return bike;
    }
}
