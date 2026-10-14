        package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.UserDocument;
import com.example.expo.Service.UserDocumentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
@AllArgsConstructor
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    @GetMapping("/getAllDocuments")
    public ResponseEntity<?> getAllDocuments() {
        return ResponseEntity.status(200).body(userDocumentService.getAllDocuments());
    }

    @PostMapping("/addDocument/{userId}")
    public ResponseEntity<?> addDocument(@PathVariable Integer userId, @Valid @RequestBody UserDocument document) {

        userDocumentService.addDocument(userId, document);
        return ResponseEntity.status(200).body(new ApiResponse("Document added successfully"));
    }

    @PutMapping("/updateDocument/{userId}/{documentId}")
    public ResponseEntity<?> updateDocument(@PathVariable Integer userId, @PathVariable Integer documentId, @Valid @RequestBody UserDocument document) {

        userDocumentService.updateDocument(userId, documentId, document);
        return ResponseEntity.status(200).body(new ApiResponse("Document updated successfully"));
    }

    @DeleteMapping("/deleteDocument/{userId}/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Integer userId, @PathVariable Integer documentId) {

        userDocumentService.deleteDocument(userId, documentId);
        return ResponseEntity.status(200).body(new ApiResponse("Document deleted successfully"));
    }
}
