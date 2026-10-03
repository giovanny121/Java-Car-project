package com.rental.repository;

import com.rental.model.Vehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByAvailableTrue();

    boolean existsByLicensePlate(String licensePlate);
}
