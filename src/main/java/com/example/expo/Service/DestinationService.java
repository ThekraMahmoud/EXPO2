package com.example.expo.Service;


import com.example.expo.Model.Destination;
import com.example.expo.Repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationService {
    private final DestinationRepository destinationRepository;


    // Get all destinations
    public List<Destination> getAllDestinations() {
        return destinationRepository.findAll();
    }

    // Get destination by ID
    public Destination getDestinationById(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Destination not found"));
    }

    // Add destination
    public Destination addDestination(Destination destination) {
        return destinationRepository.save(destination);
    }

    // Update destination
    public Destination updateDestination(Long id, Destination destination) {

        Destination oldDestination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        oldDestination.setName(destination.getName());
        oldDestination.setType(destination.getType());
        oldDestination.setDescription(destination.getDescription());
        oldDestination.setStatus(destination.getStatus());

        return destinationRepository.save(oldDestination);
    }

    // Delete destination
    public void deleteDestination(Long id) {

        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Destination not found"));

        destinationRepository.delete(destination);
    }
}
