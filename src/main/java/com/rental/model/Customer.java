package com.rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is required")
    @Column(nullable = false)
    private String name;

    @Min(value = 0, message = "age must be positive")
    private int age;

    private String email;

    private String phone;

    @Column(name = "license_number")
    private String licenseNumber;

    public Customer(String name, int age, String email, String phone, String licenseNumber) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
    }
}
