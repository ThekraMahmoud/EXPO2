package com.example.expo.Repository;

import com.example.expo.Model.SellerersWallets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellersWalletsRepository extends JpaRepository<SellerersWallets, Integer> {

    SellerersWallets findSellersWalletsById(Integer id);

    boolean existsBySeller_Id(Integer sellerId);


    // قفل قصير داخل معاملة الكتابة؛ لا نستخدم FOR UPDATE OF غير المتوافقة مع MariaDB.
    @org.springframework.data.jpa.repository.Query(value = "select * from sellerers_wallets where id = :id for update", nativeQuery = true)
    java.util.Optional<SellerersWallets> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);



    @org.springframework.data.jpa.repository.Query(value = "select * from sellerers_wallets where seller_id = :sellerId for update", nativeQuery = true)
    java.util.Optional<SellerersWallets> findBySellerIdForUpdate(@org.springframework.data.repository.query.Param("sellerId") Integer sellerId);

}