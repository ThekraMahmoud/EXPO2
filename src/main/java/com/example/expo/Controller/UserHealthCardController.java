        package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Service.UserHealthCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/health-cards")
@AllArgsConstructor
public class UserHealthCardController {

    private final UserHealthCardService userHealthCardService;

    @GetMapping("/getAllHealthCards")
    public ResponseEntity<?> getAllHealthCards() {
        return ResponseEntity.status(200).body(userHealthCardService.getAllHealthCards());
    }

    @PostMapping("/addHealthCard/{userId}")
    public ResponseEntity<?> addHealthCard(@PathVariable Integer userId, @Valid @RequestBody UserHealthCard healthCard) {

        userHealthCardService.addHealthCard(userId, healthCard);
        return ResponseEntity.status(200).body(new ApiResponse("Health card added successfully"));
    }

    @PutMapping("/updateHealthCard/{userId}/{healthCardId}")
    public ResponseEntity<?> updateHealthCard(@PathVariable Integer userId, @PathVariable Integer healthCardId, @Valid @RequestBody UserHealthCard healthCard) {

        userHealthCardService.updateHealthCard(userId, healthCardId, healthCard);
        return ResponseEntity.status(200).body(new ApiResponse("Health card updated successfully"));
    }

    @DeleteMapping("/deleteHealthCard/{userId}/{healthCardId}")
    public ResponseEntity<?> deleteHealthCard(@PathVariable Integer userId, @PathVariable Integer healthCardId) {

        userHealthCardService.deleteHealthCard(userId, healthCardId);
        return ResponseEntity.status(200).body(new ApiResponse("Health card deleted successfully"));
    }
}
