package com.example.expo.Model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter @Setter @Entity @Table(name="visited_places")
public class VisitedPlace {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @NotBlank @Size(max=100) @Column(nullable=false,length=100) private String placeCode;
    @NotBlank @Size(max=150) @Column(nullable=false,length=150) private String placeName;
    @Size(max=100) private String category;
    @DecimalMin("-90.0") @DecimalMax("90.0") private Double latitude;
    @DecimalMin("-180.0") @DecimalMax("180.0") private Double longitude;
    @CreationTimestamp @Column(nullable=false,updatable=false) private LocalDateTime visitedAt;
    @ManyToOne(fetch=FetchType.LAZY) @JsonIgnore @JoinColumn(name="user_id",nullable=false) private User user;
}
