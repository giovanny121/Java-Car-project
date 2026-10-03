package com.rental.service;

import com.rental.model.Customer;
import com.rental.model.Rental;
import com.rental.model.Vehicle;
import com.rental.repository.CustomerRepository;
import com.rental.repository.RentalRepository;
import com.rental.repository.VehicleRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RentalService {

    /** Extra days past the due date are billed at this multiple of the daily rate. */
    private static final double LATE_FEE_MULTIPLIER = 1.5;

    private final RentalRepository rentals;
    private final VehicleRepository vehicles;
    private final CustomerRepository customers;

    public RentalService(RentalRepository rentals, VehicleRepository vehicles, CustomerRepository customers) {
        this.rentals = rentals;
        this.vehicles = vehicles;
        this.customers = customers;
    }

    @Transactional
    public Rental rent(Long customerId, Long vehicleId, int days) {
        if (customerId == null || vehicleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId and vehicleId are required");
        }
        if (days < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental must be for at least 1 day");
        }

        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (!vehicle.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle is not available");
        }

        LocalDate start = LocalDate.now();
        LocalDate due = start.plusDays(days);
        double cost = round(days * vehicle.getRentalPricePerDay());

        Rental rental = new Rental(customer, vehicle, start, due, cost);
        vehicle.setAvailable(false);
        vehicles.save(vehicle);
        return rentals.save(rental);
    }

    @Transactional
    public Rental returnVehicle(Long rentalId) {
        Rental rental = rentals.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found"));

        if (Rental.STATUS_RETURNED.equals(rental.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental has already been returned");
        }

        LocalDate today = LocalDate.now();
        long rentedDays = Math.max(1, ChronoUnit.DAYS.between(rental.getStartDate(), today));
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(rental.getDueDate(), today));
        double dailyRate = rental.getVehicle().getRentalPricePerDay();

        double cost = rentedDays * dailyRate + lateDays * dailyRate * LATE_FEE_MULTIPLIER;

        rental.setReturnDate(today);
        rental.setTotalCost(round(cost));
        rental.setStatus(Rental.STATUS_RETURNED);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(true);
        vehicles.save(vehicle);

        return rentals.save(rental);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
