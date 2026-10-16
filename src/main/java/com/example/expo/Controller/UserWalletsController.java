
package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Service.UserWalletsService;
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
        return ResponseEntity.ok(userWalletsService.getAllWallets());
    }

    @PostMapping("/addWallet/{userId}")
    public ResponseEntity<?> addWallet(
            @PathVariable Integer userId) {

        userWalletsService.addWallet(userId);

        return ResponseEntity.ok(
                new ApiResponse("Wallet added successfully"));
    }

    @PutMapping("/suspendWallet/{userId}/{walletId}")
    public ResponseEntity<?> suspendWallet(
            @PathVariable Integer userId,
            @PathVariable Integer walletId) {

        userWalletsService.suspendWallet(userId, walletId);

        return ResponseEntity.ok(
                new ApiResponse("Wallet suspended successfully"));
    }

    @PutMapping("/activateWallet/{userId}/{walletId}")
    public ResponseEntity<?> activateWallet(
            @PathVariable Integer userId,
            @PathVariable Integer walletId) {

        userWalletsService.activateWallet(userId, walletId);

        return ResponseEntity.ok(
                new ApiResponse("Wallet activated successfully"));
    }
    @PostMapping("/test-top-up/{userId}/{walletId}")
    public ResponseEntity<?> testTopUp(@PathVariable Integer userId, @PathVariable Integer walletId) {

        userWalletsService.creditWalletAfterVerifiedTopUp(
                userId,
                walletId,
                new java.math.BigDecimal("100.00"),
                "TEST-TOPUP-" + java.util.UUID.randomUUID()
        );

        return ResponseEntity.ok(
                new ApiResponse("Test top-up successful: 100 SAR")
        );
    }
}
