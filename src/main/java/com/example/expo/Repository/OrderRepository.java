package com.example.expo.Repository;

import com.example.expo.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    java.util.List<Order> findAllByUser_IdOrderByCreatedAtDesc(Integer userId);
    java.util.List<Order> findAllBySeller_IdOrderByCreatedAtDesc(Integer sellerId);
}
