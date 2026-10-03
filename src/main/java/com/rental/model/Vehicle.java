package com.rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "license plate is required")
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @NotBlank(message = "vehicle type is required")
    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    private String category;

    @DecimalMin(value = "0.0", message = "rental price must be positive")
    @Column(name = "rental_price_per_day", nullable = false)
    private double rentalPricePerDay;

    @Column(nullable = false)
    private boolean available = true;

    public Vehicle(String licensePlate, String vehicleType, String category, double rentalPricePerDay) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.category = category;
        this.rentalPricePerDay = rentalPricePerDay;
        this.available = true;
    }
}
