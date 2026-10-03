package com.rental.repository;

import com.rental.model.Rental;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByStatus(String status);

    boolean existsByCustomerId(Long customerId);

    boolean existsByVehicleId(Long vehicleId);
}
