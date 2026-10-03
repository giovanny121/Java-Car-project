package com.rental.config;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;
import com.rental.repository.CustomerRepository;
import com.rental.repository.RentalRepository;
import com.rental.repository.VehicleRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds a small demo dataset on first start so the dashboard is not empty.
 * Does nothing once data already exists.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customers;
    private final VehicleRepository vehicles;
    private final RentalRepository rentals;

    public DataSeeder(CustomerRepository customers, VehicleRepository vehicles, RentalRepository rentals) {
        this.customers = customers;
        this.vehicles = vehicles;
        this.rentals = rentals;
    }

    @Override
    public void run(String... args) {
        if (customers.count() > 0 || vehicles.count() > 0) {
            return;
        }

        Customer jeremy = customers.save(new Customer("Jeremy", 30, "jeremy@example.com", "555-0101", "LIC-1001"));
        Customer wfi = customers.save(new Customer("Wfi", 35, "wfi@example.com", "555-0102", "LIC-1002"));
        Customer jemina = customers.save(new Customer("Jemina", 28, "jemina@example.com", "555-0103", "LIC-1003"));
        Customer daniel = customers.save(new Customer("Daniel", 41, "daniel@example.com", "555-0104", "LIC-1004"));

        Vehicle sedan = vehicles.save(new Vehicle("UBM45", "CAR", "Sedan", 52.5));
        Vehicle sportBike = vehicles.save(new Vehicle("UBJ78", "MOTORCYCLE", "SportBike", 30.1));
        Vehicle suv = vehicles.save(new Vehicle("UBX99", "CAR", "SUV", 78.0));

        // A completed rental (contributes to revenue)
        LocalDate start1 = LocalDate.now().minusDays(10);
        Rental returned = new Rental(wfi, sportBike, start1, start1.plusDays(3), 3 * sportBike.getRentalPricePerDay());
        returned.setReturnDate(start1.plusDays(3));
        returned.setStatus(Rental.STATUS_RETURNED);
        rentals.save(returned);

        // An active rental (marks the sedan as unavailable)
        LocalDate start2 = LocalDate.now().minusDays(2);
        rentals.save(new Rental(jeremy, sedan, start2, start2.plusDays(5), 5 * sedan.getRentalPricePerDay()));
        sedan.setAvailable(false);
        vehicles.save(sedan);
    }
}
