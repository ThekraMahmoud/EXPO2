
package com.example.expo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clinic_branches")
@Getter
@Setter
public class ClinicBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Branch name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Zone is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String zone;

    @NotBlank(message = "Location description is required")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String locationDescription;

    @NotBlank(message = "Phone number is required")
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String email;

    @Size(max = 100)
    @Column(length = 100)
    private String openingHours;

    @Column(columnDefinition = "TEXT")
    private String servicesDescription;
}
