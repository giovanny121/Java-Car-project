package com.rental.controller;

import com.rental.dto.RentalRequest;
import com.rental.model.Rental;
import com.rental.repository.RentalRepository;
import com.rental.service.RentalService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalRepository rentals;
    private final RentalService rentalService;

    public RentalController(RentalRepository rentals, RentalService rentalService) {
        this.rentals = rentals;
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Rental> all() {
        return rentals.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rental rent(@RequestBody RentalRequest request) {
        int days = request.days() == null ? 1 : request.days();
        return rentalService.rent(request.customerId(), request.vehicleId(), days);
    }

    @PostMapping("/{id}/return")
    public Rental returnVehicle(@PathVariable Long id) {
        return rentalService.returnVehicle(id);
    }
}
