package com.example.expo.Repository;

import com.example.expo.Model.VisitedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitedPlaceRepository extends JpaRepository<VisitedPlace,Integer> {
    List<VisitedPlace> findAllByUser_IdOrderByVisitedAtDesc(Integer userId);
    boolean existsByUser_IdAndPlaceCode(Integer userId, String placeCode);
}
