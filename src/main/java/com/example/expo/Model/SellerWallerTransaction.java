        package com.example.expo.Model;

import com.example.expo.Model.SellerersWallets;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SellerWallerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // TOP_UP: إضافة مستحقات البائع
    // BANK_TRANSFER: تحويل إلى حساب البائع البنكي
    // REFUND: استرداد مبلغ لعميل
    @NotBlank
    @Pattern(regexp = "TOP_UP|BANK_TRANSFER|REFUND")
    @Column(nullable = false, length = 20)
    private String type;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @NotBlank
    @Pattern(regexp = "PENDING|SUCCESS|FAILED")
    @Column(nullable = false, length = 10)
    private String status;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_wallet_id", nullable = false)
    @JsonIgnore
    private SellerersWallets sellerWallet;
}