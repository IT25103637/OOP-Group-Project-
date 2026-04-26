/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

// ============================================================
// Car — Subclass of Vehicle (Polymorphism)
// Economy pricing: flat rate per day.
// Late fee: 20% surcharge per late day.
//
// CSV extra field: numberOfSeats
// ============================================================
public class Car extends Vehicle {

    private int numberOfSeats;

    public Car() {}

    public Car(String id, String brand, String model,
               double pricePerDay, String location, int numberOfSeats) {
        super(id, "CAR", brand, model, pricePerDay, location);
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    // Member 05: Polymorphic price calculation — Cars use flat rate
    @Override
    public double calculateRentalPrice(int days) {
        return getPricePerDay() * days;
    }

    // Member 03: Late fee — 20% extra per late day
    @Override
    public double calculateLateFee(int lateDays) {
        return getPricePerDay() * 1.2 * lateDays;
    }

    @Override
    public String getVehicleDescription() {
        return "Car: " + getBrand() + " " + getModel() + " (" + numberOfSeats + " seats)";
    }

    // Convert to CSV line — common fields + numberOfSeats at end
    @Override
    public String toCsv() {
        return baseCsv() + "," + numberOfSeats + imageNameCsv();
    }

    // Build a Car object from a CSV line
    public static Car fromCsv(String csvLine) {
        // Format: id,CAR,brand,model,pricePerDay,location,availableStatus,numberOfSeats[,imageName]
        String[] p = csvLine.split(",");
        Car car = new Car(p[0], p[2], p[3], Double.parseDouble(p[4]), p[5], Integer.parseInt(p[7]));
        car.setAvailableStatus(Boolean.parseBoolean(p[6]));
        if (p.length > 8 && p[8] != null && !p[8].trim().isEmpty()) {
            car.setImageName(p[8].trim());
        }
        return car;
    }
}
