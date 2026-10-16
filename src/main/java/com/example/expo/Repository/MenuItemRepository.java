package com.example.expo.Repository;

import com.example.expo.Model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    java.util.List<MenuItem> findAllByMenu_Id(Integer menuId);
    // يعرض المنتجات التي تحتاج مراجعة أو التي عطلها البائع، ضمن قائمة محددة.
    java.util.List<MenuItem> findAllByMenu_IdAndAvailableFalse(Integer menuId);
    java.util.List<MenuItem> findAllByMenu_Seller_IdAndAvailableFalse(Integer sellerId);
}
