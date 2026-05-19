package com.rental.rashdeen;

import com.rental.sandaru.LuxuryVehicleFee;
import com.rental.sandaru.StandardVehicleFee;



import org.springframework.stereotype.Component;


@Component
public class LateFeeFactory {

    
    public LateFeeCalculator getCalculator(String vehicleBrand) {
        if (vehicleBrand == null || vehicleBrand.isBlank()) {
            return new StandardVehicleFee(); // safe fallback
        }

        // Check the brand string (case-insensitive) against every known luxury keyword.
        String brandUpper = vehicleBrand.toUpperCase();
        for (String luxuryKeyword : LuxuryVehicleFee.LUXURY_BRANDS) {
            if (brandUpper.contains(luxuryKeyword)) {
                return new LuxuryVehicleFee(); 
            }
        }

        return new StandardVehicleFee(); 
    }
}
