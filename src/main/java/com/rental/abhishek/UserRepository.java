/**
 * Component 02: Repositories & Data Access
 * @author Abhishek (it25102355@my.sliit.lk)
 */
package com.rental.abhishek;

import com.rental.sithika.FileService;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class UserRepository {

    private static final String FILENAME = "users.txt";

    @Autowired
    private FileService fileService;

    // ---- READ ALL users ----
    public List<User> findAll() {
        List<String> lines = fileService.readLines(FILENAME);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            User u = User.fromCsv(line);
            if (u != null) users.add(u);
        }
        return users;
    }

  
    public Optional<User> findById(String id) {
        return findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

  
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    
    public long count() {
        return findAll().size();
    }

   
    public void save(User user) {
        List<String> lines = fileService.readLines(FILENAME);
        String newLine = user.toCsv();
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            // split with limit=2 so a comma in the name field doesn't break the ID check
            String[] parts = lines.get(i).split(",", 2);
            if (parts.length > 0 && parts[0].trim().equals(user.getId())) {
                lines.set(i, newLine); // UPDATE existing record in-place
                found = true;
                break;
            }
        }

        if (!found) {
            
            fileService.appendLine(FILENAME, newLine);
        } else {
            
            fileService.writeLines(FILENAME, lines);
        }
    }

   
    public void delete(String id) {
        List<String> lines = fileService.readLines(FILENAME);
        lines.removeIf(line -> line.split(",")[0].equals(id));
        fileService.writeLines(FILENAME, lines);
    }
}
