
package com.rental.nichala;



public abstract class Vehicle {

    private String id;           // UUID string
    private String type;         // CAR, BIKE, VAN, TRUCK
    private String brand;        // e.g. "Toyota"
    private String model;        // e.g. "Corolla"
    private double pricePerDay;
    private String location;
    private boolean availableStatus;
    private String imageName;    

   
    public Vehicle() {}

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

    
   
    public abstract double calculateRentalPrice(int days);
    public abstract double calculateLateFee(int lateDays);
    public abstract String getVehicleDescription();

  
    
    public abstract String toCsv();

    
    protected String baseCsv() {
        return id + "," + type + "," + brand + "," + model + ","
                + pricePerDay + "," + location + "," + availableStatus;
    }

    
    protected String imageNameCsv() {
        return (imageName != null && !imageName.isEmpty()) ? "," + imageName : ",";
    }

    
    public void toggleStatus() {
        this.availableStatus = !this.availableStatus;
    }

    
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
