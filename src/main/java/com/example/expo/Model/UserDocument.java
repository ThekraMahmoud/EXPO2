package com.example.expo.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(nullable = false, length = 50)
    @NotBlank(message = " please Enter documentType")
    private String documentType;


    @Column(nullable = false ,length = 250)
    @NotBlank(message = " please Enter fileUrl")
    private String fileUrl;

    private LocalDate expiryDate;


    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;



    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private User user;


}
