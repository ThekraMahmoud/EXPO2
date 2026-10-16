package com.example.expo.Model;


import com.example.expo.Enum.NodeType;
import com.example.expo.Model.Destination;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(nullable = false, length = 100)
    private String name;

    private Double x;

    private Double y;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType nodeType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }



}
