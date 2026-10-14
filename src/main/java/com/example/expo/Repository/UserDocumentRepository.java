package com.example.expo.Repository;

import com.example.expo.Model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument,Integer> {
UserDocument findUserDocumentById(Integer id);
}
