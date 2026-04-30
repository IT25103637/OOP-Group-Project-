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


public class User extends BaseUser {

    // Default constructor (needed by Spring for form binding)
    public User() {}

    // Full constructor
    public User(String id, String name, String email, String password, Role role) {
        super(id, name, email, password, role);
    }

    // Inherited abstract method — returns the correct dashboard URL based on role
    @Override
    public String getDashboardUrl() {
        if (role == Role.ADMIN) {
            return "/admin/dashboard";
        }
        return "/vehicles";
    }


    public String toCsv() {
        // Format: id,name,email,password,role
        return id + "," + name + "," + email + "," + password + "," + role.name();
    }

    // Read a CSV line from the text file and build a User object
    public static User fromCsv(String csvLine) {
        String[] parts = csvLine.split(",", 5); // limit=5 to handle commas in names
        if (parts.length < 5) return null;      // skip bad lines
        return new User(
            parts[0],                    // id
            parts[1],                    // name
            parts[2],                    // email
            parts[3],                    // password
            Role.valueOf(parts[4])       // role (ADMIN / CUSTOMER)
        );
    }
}
