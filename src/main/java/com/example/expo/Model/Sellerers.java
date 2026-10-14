package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Sellerers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String business_name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone must contain exactly 10 digits"
    )
    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)//يمنع ظهور الباسوورد في الجيت
    @NotBlank(message = "Password hash is required")
    @Size(max = 255, message = "Password hash must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "Commercial registration is required")
    @Size(max = 50, message = "Commercial registration must not exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String commercial_register;

    @NotBlank(message = "Verification status is required")
    @Pattern(
            regexp = "PENDING|VERIFIED|REJECTED",
            message = "Verification status must be PENDING, VERIFIED, or REJECTED"
    )
    @Column(nullable = false, length = 8)
    private String verification_status;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "ACTIVE|INACTIVE",
            message = "Status must be ACTIVE or INACTIVE"
    )
    @Column(nullable = false, length = 8)
    private String status;

    @NotNull(message = "Created date is required")
    @PastOrPresent(message = "Created date cannot be in the future")
    @Column(nullable = false, updatable = false)
    private LocalDate created_at;

    @NotNull(message = "Updated date is required")
    @PastOrPresent(message = "Updated date cannot be in the future")
    @Column(nullable = false)
    private LocalDate updated_at;

    /////////////////Relation

    //لكل بائع اكثر من فاتورة
    @JsonIgnore
    @OneToMany(mappedBy = "seller")
    private List<Invoices> invoices;

    //لكل بائع محفظة واحدة كحد اقصى
    @JsonIgnore
    @OneToOne(mappedBy = "seller")
    private SellerersWallets sellerersWallets;
}