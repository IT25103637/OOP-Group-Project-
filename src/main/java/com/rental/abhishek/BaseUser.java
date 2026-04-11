/**
 * Component 01: Models & Entities
 * @author Nichala (it25102056@my.sliit.lk)
 */
package com.rental.abhishek;


public abstract class BaseUser {

    protected String id;       
    protected String name;
    protected String email;
    protected String password;
    protected Role role;       // ADMIN or CUSTOMER

    // Default constructor
    public BaseUser() {}

    // Full constructor
    public BaseUser(String id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Abstract method — every subclass must say where its dashboard is
    public abstract String getDashboardUrl();

    // ---------- Getters & Setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
