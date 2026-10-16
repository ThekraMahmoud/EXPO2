package com.example.expo.Repository;

import com.example.expo.Enum.DestinationStatus;
import com.example.expo.Model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    long countByStatus(DestinationStatus status);
}

