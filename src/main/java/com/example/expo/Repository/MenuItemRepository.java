package com.example.expo.Repository;

import com.example.expo.Model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    java.util.List<MenuItem> findAllByMenu_Id(Integer menuId);
}
