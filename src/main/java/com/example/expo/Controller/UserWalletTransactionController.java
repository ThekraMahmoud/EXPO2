
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
}