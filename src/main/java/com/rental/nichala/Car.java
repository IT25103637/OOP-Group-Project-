/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;


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

    @Override
    public double calculateLateFee(int lateDays) {
        return getPricePerDay() * 1.2 * lateDays;
    }

    @Override
    public String getVehicleDescription() {
        return "Car: " + getBrand() + " " + getModel() + " (" + numberOfSeats + " seats)";
    }

    @Override
    public String toCsv() {
        return baseCsv() + "," + numberOfSeats + imageNameCsv();
    }

   
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
