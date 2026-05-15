/**
 * Component 03: Business Logic & Services
 * @author Rashdeen (it25103637@my.sliit.lk)
 */
package com.rental.sandaru;

import com.rental.rashdeen.LateFeeCalculator;



import org.springframework.stereotype.Component;


@Component("standardVehicleFee")
public class StandardVehicleFee implements LateFeeCalculator {

    @Override
    public double calculateFee(long daysLate, double pricePerDay) {
        if (daysLate <= 0) return 0.0;
        return pricePerDay * 1.2 * daysLate;
    }
}
