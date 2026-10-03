package com.rental.dto;

public record RentalRequest(Long customerId, Long vehicleId, Integer days) {
}
