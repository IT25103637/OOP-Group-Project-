/**
 * Component 03: Business Logic & Services
 * @author Rashdeen (it25103637@my.sliit.lk)
 */
package com.rental.rashdeen;



public interface LateFeeCalculator {

    double calculateFee(long daysLate, double pricePerDay);
}
