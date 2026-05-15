/**
 * Component 02: Repositories & Data Access
 * @author Abhishek (it25102355@my.sliit.lk)
 */
package com.rental.rashdeen;

import com.rental.sithika.FileService;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class BookingRepository {

    private static final String FILENAME = "bookings.txt";

    @Autowired
    private FileService fileService;

    // READ ALL bookings
    public List<Booking> findAll() {
        List<String> lines = fileService.readLines(FILENAME);
        List<Booking> bookings = new ArrayList<>();

        for (String line : lines) {
            Booking b = Booking.fromCsv(line);
            if (b != null) bookings.add(b);
        }
        return bookings;
    }

    //READ by booking ID 
    public Optional<Booking> findById(String id) {
        return findAll().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }

    //READ all bookings belonging to a specific user 
    public List<Booking> findByUserId(String userId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : findAll()) {
            if (b.getUserId().equals(userId)) {
                result.add(b);
            }
        }
        return result;
    }

    //SAVE (INSERT or UPDATE)
    public void save(Booking booking) {
        List<String> lines = fileService.readLines(FILENAME);
        String newLine = booking.toCsv();
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            String existingId = lines.get(i).split(",")[0];
            if (existingId.equals(booking.getId())) {
                lines.set(i, newLine); // UPDATE existing booking (e.g. on return)
                found = true;
                break;
            }
        }

        if (!found) {
            fileService.appendLine(FILENAME, newLine); // INSERT new booking
        } else {
            fileService.writeLines(FILENAME, lines);   // Rewrite updated file
        }
    }

    public void deleteById(String id) {
        List<String> lines = fileService.readLines(FILENAME);
        // Keep every line whose first CSV field does NOT match the cancelled ID
        List<String> updated = lines.stream()
                .filter(line -> !line.startsWith(id + ","))
                .toList();
        fileService.writeLines(FILENAME, updated);
    }
}
