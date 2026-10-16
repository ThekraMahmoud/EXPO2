package com.example.expo.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** A manual-location assistance request; no medical-card data is copied into it. */
@Entity
@Table(name = "paramedic_requests", indexes = {
    @Index(name = "idx_paramedic_user", columnList = "user_id"),
    @Index(name = "idx_paramedic_status", columnList = "status")
})
@Getter @Setter
public class ParamedicRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id", nullable = false) private Integer userId;
    @Column(nullable = false, length = 255) private String visitorName;
    @Column(nullable = false, length = 500) private String locationDescription;
    @Column(length = 1000) private String note;
    // Optional for existing self-reported requests; mandatory for public QR reports.
    @Column(length = 100) private String reporterName;
    @Column(length = 30) private String reporterPhone;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    private Integer clinicId;
    @Column(length = 100) private String clinicName;
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime closedAt;
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
}
