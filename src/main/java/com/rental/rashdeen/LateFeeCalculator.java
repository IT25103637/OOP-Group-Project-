
package com.rental.rashdeen;



public interface LateFeeCalculator {

    double calculateFee(long daysLate, double pricePerDay);
}
