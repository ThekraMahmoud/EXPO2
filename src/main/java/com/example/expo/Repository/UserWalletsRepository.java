        package com.example.expo.Repository;

import com.example.expo.Model.UserWallets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWalletsRepository
        extends JpaRepository<UserWallets, Integer> {

    UserWallets findUserWalletsById(Integer id);

    UserWallets findUserWalletsByUserId(Integer userId);
}
