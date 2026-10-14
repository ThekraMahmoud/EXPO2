package com.example.expo.Repository;

import com.example.expo.Model.UserHealthCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserHealthCardRepository extends JpaRepository<UserHealthCard,Integer> {

    UserHealthCard findUserHealthCardByUserId(Integer userId);
    UserHealthCard findUserHealthCardById(Integer id);

}
