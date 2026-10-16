package com.example.expo.Repository;

import com.example.expo.Model.ParamedicRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ParamedicRequestRepository extends JpaRepository<ParamedicRequest, Integer> {
    java.util.Optional<ParamedicRequest> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(Integer userId, List<String> statuses);
    boolean existsByClinicIdAndStatus(Integer clinicId, String status);
    List<ParamedicRequest> findTop100ByUserIdOrderByCreatedAtDesc(Integer userId);
    boolean existsByUserIdAndStatusIn(Integer userId, List<String> statuses);
    List<ParamedicRequest> findTop100ByStatusOrClinicIdOrderByCreatedAtDesc(String status, Integer clinicId);

    // A single conditional update makes competing clinic acceptances mutually exclusive.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ParamedicRequest r set r.status='ACCEPTED', r.clinicId=:clinicId, r.clinicName=:name, r.acceptedAt=:time where r.id=:id and r.status='PENDING'")
    int claim(@Param("id") Integer id, @Param("clinicId") Integer clinicId,
              @Param("name") String name, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ParamedicRequest r set r.status='CANCELLED', r.closedAt=:time where r.id=:id and r.userId=:userId and r.status='PENDING'")
    int cancel(@Param("id") Integer id, @Param("userId") Integer userId, @Param("time") LocalDateTime time);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ParamedicRequest r set r.status='COMPLETED', r.closedAt=:time where r.id=:id and r.clinicId=:clinicId and r.status='ACCEPTED'")
    int complete(@Param("id") Integer id, @Param("clinicId") Integer clinicId, @Param("time") LocalDateTime time);
}
