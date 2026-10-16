
package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Service.SellersWalletsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/seller-wallet")
public class SellersWalletsController {

    private final SellersWalletsService sellersWalletsService;

    // عرض جميع المحافظ
    @GetMapping("/get")
    public ResponseEntity<?> getAllWallets() {
        return ResponseEntity.ok(
                sellersWalletsService.getAllWallets()
        );
    }

    // عرض محفظة بائع محدد
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getWalletBySellerId(@PathVariable Integer sellerId) {
        return ResponseEntity.ok(sellersWalletsService.getWalletBySellerId(sellerId)
        );
    }

    // إنشاء محفظة لبائع
    @PostMapping("/add/{sellerId}")
    public ResponseEntity<?> addWallet(@PathVariable Integer sellerId) {
        sellersWalletsService.addWallet(sellerId);
        return ResponseEntity.status(201).body(new ApiResponse("Wallet created successfully"));
    }

    // إيقاف المحفظة أو تفعيلها
    @PutMapping("/update/{walletId}")
    public ResponseEntity<?> updateWallet(@PathVariable Integer walletId, @RequestParam String status) {

        if ("ACTIVE".equalsIgnoreCase(status)) {
            sellersWalletsService.activateWallet(walletId);
        } else if ("SUSPENDED".equalsIgnoreCase(status)) {
            sellersWalletsService.suspendWallet(walletId);
        } else {
            throw new IllegalArgumentException(
                    "Status must be ACTIVE or SUSPENDED"
            );
        }
        return ResponseEntity.ok(new ApiResponse("Wallet status updated successfully"));
    }

    // طلب تحويل بنكي
    @PostMapping("/transfer/{walletId}")
    public ResponseEntity<?> requestBankTransfer(@PathVariable Integer walletId, @RequestParam BigDecimal amount) {

        return ResponseEntity.status(201).body(sellersWalletsService.requestBankTransfer(walletId, amount
                )
        );
    }

    // عرض معاملات المحفظة
    @GetMapping("/transactions/{walletId}")
    public ResponseEntity<?> getWalletTransactions(
            @PathVariable Integer walletId) {

        return ResponseEntity.ok(
                sellersWalletsService.getWalletTransactions(walletId)
        );
    }
}
