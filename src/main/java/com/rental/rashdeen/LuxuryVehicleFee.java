/**
 * Component 03: Business Logic & Services
 * @author Rashdeen (it25103637@my.sliit.lk)
 */
package com.rental.rashdeen;

import com.rental.nichala.*;
import com.rental.abhishek.*;
import com.rental.rashdeen.*;
import com.rental.nisal.*;
import com.rental.sandaru.*;
import com.rental.sithika.*;

import org.springframework.stereotype.Component;


@Component("luxuryVehicleFee")
public class LuxuryVehicleFee implements LateFeeCalculator {

    // Luxury brands: vehicles whose brand string contains any of these keywords.
   
    public static final String[] LUXURY_BRANDS = {
        "BMW", "BENZ", "MERCEDES", "AUDI", "PORSCHE", "LEXUS", "JAGUAR"
    };

    // Luxury penalty: 250% of the daily rate per late day.
    // The 2.5x multiplier reflects the premium insurance and
    @Override
    public double calculateFee(long daysLate, double pricePerDay) {
        if (daysLate <= 0) return 0.0;
        return pricePerDay * 2.5 * daysLate;
    }
}
