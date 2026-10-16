package com.example.expo.Model;


import com.example.expo.Enum.PathType;
import com.example.expo.Model.MapNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "start_node_id", nullable = false)
    private MapNode startNode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "end_node_id", nullable = false)
    private MapNode endNode;

    @Column(nullable = false)
    private Double distance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PathType pathType;

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

