 package com.example.expo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class UserHealthCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$")
    @Column(length = 3)
    private String bloodType;

    @Size(max = 500)
    @Column(length = 500)
    private String allergies;

    @Size(max = 500)
    @Column(length = 500)
    private String chronicDiseases;

    @Size(max = 100)
    @Column(length = 100)
    private String emergencyContactName;

    @Pattern(regexp = "^(05\\d{8}|9665\\d{8})$")
    @Column(length = 12)
    private String emergencyContactPhone;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false,unique = true
    )
    private User user;
}