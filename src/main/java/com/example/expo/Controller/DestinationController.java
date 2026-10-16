package com.example.expo.Controller;


import com.example.expo.Model.Destination;
import com.example.expo.Service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/destinations")
@RequiredArgsConstructor
public class DestinationController {


    private final DestinationService destinationService;


    // Get all destinations
    @GetMapping
    public ResponseEntity<?> getAllDestinations() {
        return ResponseEntity.status(200)
                .body(destinationService.getAllDestinations());
    }

    // Get destination by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDestinationById(@PathVariable Long id) {
        return ResponseEntity.status(200)
                .body(destinationService.getDestinationById(id));
    }

    // Add destination
    @PostMapping
    public ResponseEntity<?> addDestination(
            @RequestBody Destination destination) {
        return ResponseEntity.status(201)
                .body(destinationService.addDestination(destination));
    }

    // Update destination
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDestination(
            @PathVariable Long id,
            @RequestBody Destination destination) {
        return ResponseEntity.status(200)
                .body(destinationService.updateDestination(id, destination));
    }

    // Delete destination
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.status(200).body("Destination deleted successfully");
    }}


