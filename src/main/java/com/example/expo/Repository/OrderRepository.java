package com.example.expo.Repository;

import com.example.expo.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    java.util.List<Order> findAllByUser_IdOrderByCreatedAtDesc(Integer userId);
    java.util.List<Order> findAllBySeller_IdOrderByCreatedAtDesc(Integer sellerId);


    // قفل قصير داخل معاملة الكتابة؛ لا نستخدم FOR UPDATE OF غير المتوافقة مع MariaDB.
    @org.springframework.data.jpa.repository.Query(value = "select * from orders where id = :id for update", nativeQuery = true)
    java.util.Optional<Order> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);

}
