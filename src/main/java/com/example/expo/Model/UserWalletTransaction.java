package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity

@Check(constraints = "amount > 0")
@Check(constraints = "transaction_type IN ('TOP_UP', 'PAYMENT', 'REFUND', 'SELLER_CREDIT')")
@Check(constraints = "status IN ('PENDING', 'SUCCESS', 'FAILED')")

public class UserWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;




    @Pattern(
            regexp = "^(TOP_UP|PAYMENT|REFUND|SELLER_CREDIT)$",
            message = "Invalid transaction type"
    )
    @Column(nullable = false)
    private String transactionType;

    @Positive(message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount ;


    @Pattern(regexp = "^(PENDING|SUCCESS|FAILED)$")
    @Column(nullable = false)
    private String status;


    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @CreationTimestamp
    @Column(nullable = false ,updatable = false)
    private LocalDateTime createdAt;






    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private UserWallets userWallets;
}
