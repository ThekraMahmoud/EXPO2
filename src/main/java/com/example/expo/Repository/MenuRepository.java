package com.example.expo.Repository;

import com.example.expo.Model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    java.util.List<Menu> findAllBySeller_Id(Integer sellerId);
}
