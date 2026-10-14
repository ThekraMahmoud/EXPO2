        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserDocument;
import com.example.expo.Repository.UserDocumentRepository;
import com.example.expo.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserDocumentService {

    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;

    public List<UserDocument> getAllDocuments() {
        return userDocumentRepository.findAll();
    }

    public void addDocument(Integer userId, UserDocument document) {

        User user = userRepository.findUserById(userId);
        if(user==null){
            throw new ApiException("User not found");
        }
        document.setUser(user);
        userDocumentRepository.save(document);
    }

    public void updateDocument(Integer userId,Integer documentId, UserDocument document) {

        User user = userRepository.findUserById(userId);
        if(user==null){
            throw new ApiException("User not found");
        }
        UserDocument oldDocument = userDocumentRepository.findUserDocumentById(documentId);
        if(oldDocument==null) {
           throw new ApiException ("Document not found");
        }

        oldDocument.setDocumentType(document.getDocumentType());
        oldDocument.setFileUrl(document.getFileUrl());
        oldDocument.setExpiryDate(document.getExpiryDate());
        userDocumentRepository.save(oldDocument);
    }

    public void deleteDocument(Integer userId,Integer documentId) {
        User user = userRepository.findUserById(userId);
        if(user==null){
            throw new ApiException("User not found");
        }
        UserDocument document = userDocumentRepository.findUserDocumentById(documentId);
        if(document ==null) {
            throw new ApiException ("Document not found");
        }
        userDocumentRepository.delete(document);
    }
}
