package com.example.expo.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Getter
@Setter
@NoArgsConstructor
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 150)
    private String zone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 1000)
    private String healthCardUrl;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(nullable = false, length = 20)
    private String emailStatus = "PENDING";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (emailStatus == null) {
            emailStatus = "PENDING";
        }
    }
}