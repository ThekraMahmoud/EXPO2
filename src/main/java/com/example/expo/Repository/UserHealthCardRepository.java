package com.example.expo.Repository;

import com.example.expo.Model.UserHealthCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface UserHealthCardRepository extends JpaRepository<UserHealthCard,Integer> {

    UserHealthCard findUserHealthCardByUserId(Integer userId);
    UserHealthCard findUserHealthCardById(Integer id);

    Optional<UserHealthCard> findByPublicToken(String publicToken);

    // قفل صف البطاقة يمنع إصدار توكنين مختلفين لأول طلبين متزامنين.
    // صيغة FOR UPDATE مشتركة بين MariaDB وMySQL، دون FOR UPDATE OF.
    @Query(value = "select * from user_health_card where user_id = :userId for update", nativeQuery = true)
    Optional<UserHealthCard> findByUserIdForQr(@Param("userId") Integer userId);

}
