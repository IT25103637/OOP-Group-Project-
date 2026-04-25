# Vehicle Rental Service Platform

This is a Spring Boot web application built for an OOP University Project.

## Tech Stack
* **Backend:** Java 17, Spring Boot 3.x, Spring MVC, Spring Data JPA, Hibernate, Maven
* **Database:** Microsoft SQL Server (SQLEXPRESS)
* **Frontend:** Thymeleaf, Bootstrap 5 (via CDN), HTML5

## Object-Oriented Principles Implemented
1. **Encapsulation:** All fields in entities are `private` with public `getter` and `setter` methods.
2. **Inheritance:** An abstract `Vehicle` base class is inherited by `Car`, `Bike`, `Van`, and `Truck` entities. Mapped to the database using `SINGLE_TABLE` inheritance strategy.
3. **Polymorphism:** The `calculateRentalPrice()`, `getVehicleDescription()`, and `calculateLateFee()` methods are defined in the abstract `Vehicle` class and overridden in each subclass to provide vehicle-specific logic.
4. **Abstraction:** Core business rules are hidden behind Service layer interfaces (e.g., `VehicleService`, `BookingService`), separating logic from Controllers and Repositories.

## Setup Instructions

### 1. Database Setup (SQL Server)
1. Ensure Microsoft SQL Server and SQL Server Management Studio (SSMS) are installed.
2. Open SSMS and connect to `localhost\SQLEXPRESS` (Windows Authentication).
3. Execute the following SQL to create the database:
   ```sql
   CREATE DATABASE VehicleRentalDB;
   ```
   *(Note: The application is configured with `ddl-auto=update`, so Spring Boot will automatically generate the required tables: `users`, `vehicles`, `bookings` upon startup.)*

### 2. Running in IDEs

#### IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Click **File -> Open** and select the `d:\OOP Project` folder.
3. Wait for Maven to download the dependencies.
4. Open `src/main/java/com/rental/VehicleRentalApplication.java`.
5. Click the green **Run** button next to the `main` method.

#### Eclipse / STS (Spring Tool Suite)
1. Go to **File -> Import -> Maven -> Existing Maven Projects**.
2. Select the `d:\OOP Project` directory and click **Finish**.
3. Right-click on the project -> **Run As -> Spring Boot App**.

#### VS Code
1. Ensure the "Extension Pack for Java" and "Spring Boot Extension Pack" are installed.
2. Open the `d:\OOP Project` folder.
3. The Spring Boot Dashboard should appear in the side panel.
4. Expand the `VehicleRentalPlatform` app and click the **Play (Run)** icon.

### 3. Application Usage
1. Open your browser and navigate to: `http://localhost:8080`
2. **Create an Admin:** The very first user to register will automatically be assigned the `ADMIN` role.
3. **Login:** Log in with the registered credentials.
4. **Admin Dashboard:** Access the Admin Dashboard from the top navbar to add vehicles and view statistics.
5. **Customer Booking:** Register another user to act as a customer, browse vehicles, and create bookings. Admin users can process returns from the Booking History page to calculate final costs and late fees.

## Author
Developed for OOP Module Assessment.
