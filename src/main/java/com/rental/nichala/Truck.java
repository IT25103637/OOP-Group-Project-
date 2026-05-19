/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.nichala;



public class Truck extends Vehicle {

    private int payloadCapacity;

    public Truck() {}

    public Truck(String id, String brand, String model,
                 double pricePerDay, String location, int payloadCapacity) {
        super(id, "TRUCK", brand, model, pricePerDay, location);
        this.payloadCapacity = payloadCapacity;
    }

    public int getPayloadCapacity() { return payloadCapacity; }
    public void setPayloadCapacity(int payloadCapacity) { this.payloadCapacity = payloadCapacity; }

    
    @Override
    public double calculateRentalPrice(int days) {
        return (getPricePerDay() * days) + 100.0;
    }

   
    @Override
    public double calculateLateFee(int lateDays) {
        return getPricePerDay() * 2.0 * lateDays;
    }

    @Override
    public String getVehicleDescription() {
        return "Truck: " + getBrand() + " " + getModel() + " (Payload: " + payloadCapacity + " tons)";
    }

    @Override
    public String toCsv() {
        return baseCsv() + "," + payloadCapacity + imageNameCsv();
    }

    public static Truck fromCsv(String csvLine) {
        // Format: id,TRUCK,brand,model,pricePerDay,location,availableStatus,payloadCapacity[,imageName]
        String[] p = csvLine.split(",");
        Truck truck = new Truck(p[0], p[2], p[3], Double.parseDouble(p[4]), p[5], Integer.parseInt(p[7]));
        truck.setAvailableStatus(Boolean.parseBoolean(p[6]));
        if (p.length > 8 && p[8] != null && !p[8].trim().isEmpty()) {
            truck.setImageName(p[8].trim());
        }
        return truck;
    }
}
