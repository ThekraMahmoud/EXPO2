
        package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.UserWalletTransaction;
import com.example.expo.Service.UserWalletTransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallet-transactions")
@AllArgsConstructor
public class UserWalletTransactionController {

    private final UserWalletTransactionService userWalletTransactionService;

    @GetMapping("/getAllTransactions")
    public ResponseEntity<?> getAllTransactions() {
        return ResponseEntity.status(200).body(userWalletTransactionService.getAllTransactions());
    }

    @PostMapping("/addTransaction/{userId}/{walletId}")
    public ResponseEntity<?> addTransaction(
            @PathVariable Integer userId,
            @PathVariable Integer walletId,
            @Valid @RequestBody UserWalletTransaction transaction) {

        userWalletTransactionService.addTransaction(
                userId, walletId, transaction);

        return ResponseEntity.status(200)
                .body(new ApiResponse("Transaction added successfully"));
    }

    @PutMapping("/updateTransaction/{userId}/{walletId}/{transactionId}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Integer userId,
            @PathVariable Integer walletId,
            @PathVariable Integer transactionId,
            @Valid @RequestBody UserWalletTransaction transaction) {

        userWalletTransactionService.updateTransaction(
                userId, walletId, transactionId, transaction);

        return ResponseEntity.status(200)
                .body(new ApiResponse("Transaction updated successfully"));
    }

    @DeleteMapping("/deleteTransaction/{userId}/{walletId}/{transactionId}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Integer userId,
            @PathVariable Integer walletId,
            @PathVariable Integer transactionId) {

        userWalletTransactionService.deleteTransaction(
                userId, walletId, transactionId);

        return ResponseEntity.status(200)
                .body(new ApiResponse("Transaction deleted successfully"));
    }
}