package com.example.expo.Repository;

import com.example.expo.Model.Sellerers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellersRepository extends JpaRepository<Sellerers, Integer> {

    Sellerers findSellerById(Integer id);
    java.util.Optional<Sellerers> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}