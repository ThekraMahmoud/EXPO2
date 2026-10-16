package com.example.expo.Service;


import com.example.expo.Api.ApiException;
import com.example.expo.Enum.DestinationStatus;
import com.example.expo.Enum.VisitStatus;
import com.example.expo.Model.Destination;
import com.example.expo.Model.UserDestinationProgress;
import com.example.expo.Repository.DestinationRepository;
import com.example.expo.Repository.UserDestinationProgressRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDestinationService {

    private final UserDestinationProgressRepository progressRepository;
    private final DestinationRepository destinationRepository;

    // Get all progress records
    public List<UserDestinationProgress> getAllProgress() {
        return progressRepository.findAll();
    }

    // Get progress by ID
    public UserDestinationProgress getProgressById(Long id) {
        return progressRepository.findById(id).orElseThrow(() -> new ApiException("Progress record not found with id: " + id));
    }

    // Add progress record
    public UserDestinationProgress addProgress(UserDestinationProgress progress) {
        return progressRepository.save(progress);
    }

    // Update progress record
    public UserDestinationProgress updateProgress(Long id, UserDestinationProgress progress) {

        UserDestinationProgress oldProgress = getProgressById(id);

        oldProgress.setUserId(progress.getUserId());
        oldProgress.setDestination(progress.getDestination());
        oldProgress.setStatus(progress.getStatus());
        oldProgress.setVisitedAt(progress.getVisitedAt());

        return progressRepository.save(oldProgress);
    }

    // Delete progress record
    public void deleteProgress(Long id) {
        UserDestinationProgress progress = getProgressById(id);
        progressRepository.delete(progress);
    }

    // Get all progress records for a user
    public List<UserDestinationProgress> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    // Get the user's visited destinations (Expo Passport)
    public List<UserDestinationProgress> getUserPassport(Long userId) {
        return progressRepository.findByUserIdAndStatus(
                userId, VisitStatus.VISITED);
    }

    // Mark a destination as visited
    @Transactional
    public UserDestinationProgress markAsVisited(
            Long userId, Long destinationId) {

        Destination destination = destinationRepository
                .findById(destinationId)
                .orElseThrow(() ->
                        new ApiException("Destination not found with id: " + destinationId));

        UserDestinationProgress progress = progressRepository
                .findByUserId(userId)
                .stream()
                .filter(p -> p.getDestination().getId().equals(destinationId))
                .findFirst()
                .orElseGet(() -> {
                    UserDestinationProgress newProgress =
                            new UserDestinationProgress();
                    newProgress.setUserId(userId);
                    newProgress.setDestination(destination);
                    return newProgress;
                });

        progress.setStatus(VisitStatus.VISITED);
        progress.setVisitedAt(LocalDateTime.now());

        return progressRepository.save(progress);
    }

    // Get passport progress percentage
    public double getPassportProgress(Long userId) {
        long totalDestinations = destinationRepository.countByStatus(DestinationStatus.ACTIVE);

        if (totalDestinations == 0) {
            return 0.0;
        }

        long visitedDestinations = progressRepository.countByUserIdAndStatus(userId, VisitStatus.VISITED);
        return (visitedDestinations * 100.0) / totalDestinations;
    }
}
