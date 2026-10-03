package com.rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
public class Rental {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RETURNED = "RETURNED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;

    @Column(nullable = false)
    private String status;

    public Rental(Customer customer, Vehicle vehicle, LocalDate startDate, LocalDate dueDate, double totalCost) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.totalCost = totalCost;
        this.status = STATUS_ACTIVE;
    }

    @Transient
    public boolean isOverdue() {
        return STATUS_ACTIVE.equals(status) && dueDate != null && LocalDate.now().isAfter(dueDate);
    }
}
