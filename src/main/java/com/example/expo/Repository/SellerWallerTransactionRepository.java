        package com.example.expo.Repository;

import com.example.expo.Model.SellerWallerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerWallerTransactionRepository
        extends JpaRepository<SellerWallerTransaction, Integer> {

    SellerWallerTransaction findSellerWallerTransactionById(Integer id);

    List<SellerWallerTransaction> findAllBySellerWallet_Id(Integer walletId);
}
