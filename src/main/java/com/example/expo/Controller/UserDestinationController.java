package com.example.expo.Controller;

import com.example.expo.Model.UserDestinationProgress;
import com.example.expo.Service.UserDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-destination-progress")
@RequiredArgsConstructor
public class UserDestinationController {

    private final UserDestinationService progressService;

    // Get all progress records
    @GetMapping
    public ResponseEntity<?> getAllProgress() {
        return ResponseEntity.status(200).body(progressService.getAllProgress());
    }

    // Get progress by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProgressById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(progressService.getProgressById(id));
    }

    // Add progress record
    @PostMapping
    public ResponseEntity<?> addProgress(
            @RequestBody UserDestinationProgress progress) {
        return ResponseEntity.status(201).body(progressService.addProgress(progress));
    }

    // Update progress record
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long id,
            @RequestBody UserDestinationProgress progress) {
        return ResponseEntity.status(200).body(progressService.updateProgress(id, progress));
    }

    // Delete progress record
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProgress(@PathVariable Long id) {
        progressService.deleteProgress(id);
        return ResponseEntity.status(200).body("Progress record deleted successfully");
    }

    // Get all progress records for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProgress(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(
                progressService.getUserProgress(userId));
    }

    // Get visited destinations (Expo Passport)
    @GetMapping("/user/{userId}/passport")
    public ResponseEntity<?> getUserPassport(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(
                progressService.getUserPassport(userId));
    }

    // Mark a destination as visited
    @PostMapping("/visit")
    public ResponseEntity<?> markAsVisited(
            @RequestBody Map<String, Long> request) {

        Long userId = request.get("userId");
        Long destinationId = request.get("destinationId");

        if (userId == null || destinationId == null) {
            return ResponseEntity.status(400).body("userId and destinationId are required");
        }

        return ResponseEntity.status(200).body(progressService.markAsVisited(userId, destinationId));
    }

    // Get passport progress percentage
    @GetMapping("/user/{userId}/passport/progress")
    public ResponseEntity<?> getPassportProgress(
            @PathVariable Long userId) {

        return ResponseEntity.ok(Map.of("progressPercentage",
                progressService.getPassportProgress(userId)));
    }
}

