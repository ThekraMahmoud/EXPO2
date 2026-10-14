package com.example.expo.Repository;

import com.example.expo.Model.SellerersWallets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellersWalletsRepository extends JpaRepository<SellerersWallets, Integer> {

    SellerersWallets findSellersWalletsById(Integer id);

    boolean existsBySeller_Id(Integer sellerId);
}