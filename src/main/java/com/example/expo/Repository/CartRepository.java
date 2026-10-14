package com.example.expo.Repository;

import com.example.expo.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    java.util.Optional<Cart> findByUser_IdAndSeller_IdAndStatus(Integer userId, Integer sellerId, String status);
    java.util.List<Cart> findAllByUser_Id(Integer userId);
}
