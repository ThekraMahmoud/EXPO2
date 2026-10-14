package com.example.expo.Repository;


import com.example.expo.Model.UserWalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWalletTransactionRepository extends JpaRepository<UserWalletTransaction,Integer> {
    UserWalletTransaction findUserWalletTransactionById(Integer id);
}
