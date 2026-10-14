package com.example.expo.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(
        check = @CheckConstraint(constraint = "balance >= 0")
)
public class UserWallets {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;



    @PositiveOrZero(message = "balance most be more or equal zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance=BigDecimal.ZERO;



    @Pattern(regexp = "^(ACTIVE|SUSPENDED)$")
    private String status;



    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime creteAt;


    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateAt;



     @OneToOne
     @JoinColumn(name = "user_id",nullable = false)
     private User user;



     @OneToMany(cascade = CascadeType.ALL,mappedBy = "userWallets")
     private Set<UserWalletTransaction> userWalletTransaction;

}
