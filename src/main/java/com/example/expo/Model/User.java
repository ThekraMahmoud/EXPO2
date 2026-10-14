package com.example.expo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
public class User {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;

    @Column(nullable = false , unique = true )
    @NotBlank(message = "Please Enter Unique User name ")
    private String username ;

    @Column(nullable = false,unique = true)
    @NotBlank(message = "please Enter email")
    @Email(message = "Please enter a valid email")
    private String email;

    @Column(nullable = false , unique = true)
    @NotBlank(message = "please Enter Your phone ")
    private String phone;

    @Column (nullable = false )
    @NotBlank(message = "Please enter password")
    private String password;

    @Pattern(regexp = "^(USER|ADMIN)$")
    @NotBlank(message = "please select role")
    private String role;


    @NotBlank(message = "Please select status")
    @Pattern(regexp = "^(INACTIVE|ACTIVE)$")
    private String status;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;



    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<UserDocument> documents;

    @OneToMany(cascade = CascadeType.PERSIST,mappedBy = "user")
    private Set<Invoices> invoices;

     @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
     private UserWallets wallet;

     @OneToOne(cascade = CascadeType.ALL ,mappedBy = "user")
     private UserHealthCard userHealthCard;



//     private  userDestination;

}
