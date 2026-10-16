        package com.example.expo.Repository;

import com.example.expo.Model.UserWallets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWalletsRepository
        extends JpaRepository<UserWallets, Integer> {

    UserWallets findUserWalletsById(Integer id);

    UserWallets findUserWalletsByUserId(Integer userId);


    // قفل قصير داخل معاملة الكتابة؛ لا نستخدم FOR UPDATE OF غير المتوافقة مع MariaDB.
    @org.springframework.data.jpa.repository.Query(value = "select * from user_wallets where id = :id for update", nativeQuery = true)
    java.util.Optional<UserWallets> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);



    @org.springframework.data.jpa.repository.Query(value = "select * from user_wallets where user_id = :userId for update", nativeQuery = true)
    java.util.Optional<UserWallets> findByUserIdForUpdate(@org.springframework.data.repository.query.Param("userId") Integer userId);

}
