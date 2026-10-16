package com.example.expo.Repository;

import com.example.expo.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    @org.springframework.data.jpa.repository.Query(value = "select * from user where id = :id for update", nativeQuery = true)
    java.util.Optional<User> findForParamedicRequest(@org.springframework.data.repository.query.Param("id") Integer id);
    User findUserById(Integer userId);
    java.util.Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}

