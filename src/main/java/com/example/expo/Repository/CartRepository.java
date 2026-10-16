package com.example.expo.Repository;

import com.example.expo.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    java.util.Optional<Cart> findByUser_IdAndSeller_IdAndStatus(Integer userId, Integer sellerId, String status);
    java.util.List<Cart> findAllByUser_Id(Integer userId);


    // قفل قصير داخل معاملة الكتابة؛ لا نستخدم FOR UPDATE OF غير المتوافقة مع MariaDB.
    @org.springframework.data.jpa.repository.Query(value = "select * from carts where id = :id for update", nativeQuery = true)
    java.util.Optional<Cart> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);



    @org.springframework.data.jpa.repository.Query(value = "select * from carts where user_id = :userId and seller_id = :sellerId and status = 'PENDING' for update", nativeQuery = true)
    java.util.Optional<com.example.expo.Model.Cart> findPendingForUpdate(
            @org.springframework.data.repository.query.Param("userId") Integer userId,
            @org.springframework.data.repository.query.Param("sellerId") Integer sellerId);

}
