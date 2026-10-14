package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SellerWallerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Integer id;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Type is required")
    @Pattern(
            regexp = "topup|debit|refund",
            message = "Type must be topup, debit, or refund"
    )
    @Column(nullable = false, length = 6)
    private String type;

//    @NotBlank(message = "Description is required")
//    @Size(max = 255, message = "Description must not exceed 255 characters")
//    @Column(nullable = false, length = 255)
//    private String description;

    @NotNull(message = "Created date is required")
    @PastOrPresent(message = "Created date cannot be in the future")
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;
//
//    @NotBlank(message = "Reference is required")
//    @Size(max = 100, message = "Reference must not exceed 100 characters")
//    @Column(nullable = false, unique = true, length = 100)
//    private String reference;



    /////////////////Relation

    //كل معاملة مرتبطة بمحفظة واحدة
    @NotNull(message = "Seller wallet is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_wallet_id", nullable = false)
    @JsonIgnore
    private SellerersWallets sellerWallet;
}