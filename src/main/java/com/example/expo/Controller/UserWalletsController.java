package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.UserWallets;
import com.example.expo.Service.UserWalletsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@AllArgsConstructor
public class UserWalletsController {

    private final UserWalletsService userWalletsService;

    @GetMapping("/getAllWallets")
    public ResponseEntity<?> getAllWallets() {
        return ResponseEntity.status(200).body(userWalletsService.getAllWallets());
    }

    @PostMapping("/addWallet/{userId}")
    public ResponseEntity<?> addWallet(@PathVariable Integer userId, @Valid @RequestBody UserWallets wallet) {

        userWalletsService.addWallet(userId, wallet);
        return ResponseEntity.status(200).body(new ApiResponse("Wallet added successfully"));
    }

    @PutMapping("/updateWallet/{userId}/{walletId}")
    public ResponseEntity<?> updateWallet(@PathVariable Integer userId, @PathVariable Integer walletId, @Valid @RequestBody UserWallets wallet) {

        userWalletsService.updateWallet(userId, walletId, wallet);
        return ResponseEntity.status(200).body(new ApiResponse("Wallet updated successfully"));
    }

    @DeleteMapping("/deleteWallet/{userId}/{walletId}")
    public ResponseEntity<?> deleteWallet(@PathVariable Integer userId, @PathVariable Integer walletId) {

        userWalletsService.deleteWallet(userId, walletId);
        return ResponseEntity.status(200).body(new ApiResponse("Wallet deleted successfully"));
    }
}
