package com.rental.controller;

import com.rental.model.Vehicle;
import com.rental.repository.VehicleRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicles;

    public VehicleController(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    @GetMapping
    public List<Vehicle> all(@RequestParam(required = false) Boolean available) {
        if (Boolean.TRUE.equals(available)) {
            return vehicles.findByAvailableTrue();
        }
        return vehicles.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle one(@PathVariable Long id) {
        return vehicles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@Valid @RequestBody Vehicle vehicle) {
        vehicle.setId(null);
        vehicle.setAvailable(true);
        if (vehicles.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A vehicle with that license plate already exists");
        }
        return vehicles.save(vehicle);
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @Valid @RequestBody Vehicle updated) {
        Vehicle existing = vehicles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        existing.setLicensePlate(updated.getLicensePlate());
        existing.setVehicleType(updated.getVehicleType());
        existing.setCategory(updated.getCategory());
        existing.setRentalPricePerDay(updated.getRentalPricePerDay());
        return vehicles.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!vehicles.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        vehicles.deleteById(id);
    }
}
