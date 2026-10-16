package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Invoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoice_number;


    //عندي علاقة مع الميرشنت من خلالها ممكن اجيب الميرشنت اي دي
//    @NotNull(message = "Merchant ID is required")
//    @Positive(message = "Merchant ID must be positive")
//    @Column(name = "merchant_id", insertable = false, updatable = false)
//    private Integer merchant_id;
//
//    @Positive(message = "User ID must be positive")
//    @Column(columnDefinition = "int ")
//    private Integer user_id;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column( nullable = false)
    private String description;

    @NotNull(message = "Subtotal is required")
    @PositiveOrZero(message = "Subtotal must be zero or greater")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "Tax amount is required")
    @PositiveOrZero(message = "Tax amount must be zero or greater")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal tax_amount;

    @NotNull(message = "Total amount is required")
    @PositiveOrZero(message = "Total amount must be zero or greater")
    @Digits(integer = 17, fraction = 2)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total_amount;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "PENDING|PAID|CANCELLED|EXPIRED",
            message = "Status must be PENDING, PAID, CANCELLED, or EXPIRED"
    )
    @Column(nullable = false, length = 9)
    private String status;

    @NotBlank(message = "QR token is required")
    @Size(max = 255, message = "QR token must not exceed 255 characters")
    @Column(nullable = false, unique = true)
    private String qr_token;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime expires_at;


    @PastOrPresent(message = "Paid date cannot be in the future")
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime paid_at;


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updated_at;


    /////////Relation

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "seller_id", nullable = false)//حتى لو ماكتبت اسم السيلر _اي دي هيكون مكتوب باي ديفولت
    private Sellerers seller;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore
    private User user;



}