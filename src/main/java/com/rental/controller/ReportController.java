package com.rental.controller;

import com.rental.model.Rental;
import com.rental.repository.CustomerRepository;
import com.rental.repository.RentalRepository;
import com.rental.repository.VehicleRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final RentalRepository rentals;
    private final VehicleRepository vehicles;
    private final CustomerRepository customers;

    public ReportController(RentalRepository rentals, VehicleRepository vehicles, CustomerRepository customers) {
        this.rentals = rentals;
        this.vehicles = vehicles;
        this.customers = customers;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<Rental> all = rentals.findAll();
        double totalRevenue = all.stream().mapToDouble(Rental::getTotalCost).sum();
        long active = all.stream().filter(r -> Rental.STATUS_ACTIVE.equals(r.getStatus())).count();
        long overdue = all.stream().filter(Rental::isOverdue).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCustomers", customers.count());
        summary.put("totalVehicles", vehicles.count());
        summary.put("availableVehicles", vehicles.findByAvailableTrue().size());
        summary.put("totalRentals", all.size());
        summary.put("activeRentals", active);
        summary.put("overdueRentals", overdue);
        summary.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        return summary;
    }

    @GetMapping("/revenue-by-vehicle")
    public List<Map<String, Object>> revenueByVehicle() {
        return rentals.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getVehicle().getLicensePlate()))
                .entrySet().stream()
                .map(entry -> {
                    List<Rental> list = entry.getValue();
                    double revenue = list.stream().mapToDouble(Rental::getTotalCost).sum();
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("licensePlate", entry.getKey());
                    row.put("vehicleType", list.get(0).getVehicle().getVehicleType());
                    row.put("rentals", list.size());
                    row.put("revenue", Math.round(revenue * 100.0) / 100.0);
                    return row;
                })
                .sorted(Comparator.comparingDouble((Map<String, Object> row) -> (double) row.get("revenue")).reversed())
                .toList();
    }

    @GetMapping("/overdue")
    public List<Rental> overdue() {
        return rentals.findAll().stream()
                .filter(Rental::isOverdue)
                .toList();
    }
}
