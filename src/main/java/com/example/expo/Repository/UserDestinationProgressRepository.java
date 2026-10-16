package com.example.expo.Repository;

import com.example.expo.Enum.VisitStatus;
import com.example.expo.Model.UserDestinationProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDestinationProgressRepository extends JpaRepository<UserDestinationProgress, Long> {
    // Get all progress records for a specific user
    List<UserDestinationProgress> findByUserId(Long userId);

    // Get all visited destinations for a specific user
    List<UserDestinationProgress> findByUserIdAndStatus(Long userId, VisitStatus status);

    // Count visited destinations for a specific user
    long countByUserIdAndStatus(Long userId, VisitStatus status);
}
