
package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserWallets;
import com.example.expo.Model.UserWalletTransaction;
import com.example.expo.Repository.UserRepository;
import com.example.expo.Repository.UserWalletsRepository;
import com.example.expo.Repository.UserWalletTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class UserWalletTransactionService {

    private final UserWalletTransactionRepository userWalletTransactionRepository;
    private final UserWalletsRepository userWalletsRepository;
    private final UserRepository userRepository;

    // Get all transactions - ADMIN access must be enforced
    public List<UserWalletTransaction> getAllTransactions() {
        return userWalletTransactionRepository.findAll();
    }

    // Get transactions belonging to a specific user's wallet
    public List<UserWalletTransaction> getUserTransactions(
            Integer userId, Integer walletId) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets wallet =
                userWalletsRepository.findUserWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (wallet.getUser() == null
                || !wallet.getUser().getId().equals(userId)) {
            throw new ApiException(
                    "Wallet does not belong to this user");
        }

        return userWalletTransactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getUserWallets() != null
                                && transaction.getUserWallets()
                                .getId().equals(walletId))
                .toList();
    }

    // Check whether a transaction reference already exists
    public boolean existsByReference(String reference) {
        return userWalletTransactionRepository
                .existsByReference(reference);
    }

    // Record a verified top-up
    @Transactional
    public UserWalletTransaction saveVerifiedTopUp(
            UserWallets wallet,
            BigDecimal amount,
            String reference) {

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (amount == null || amount.signum() <= 0
                || amount.scale() > 2) {
            throw new ApiException("Invalid transaction amount");
        }

        if (reference == null || reference.isBlank()) {
            throw new ApiException("Transaction reference is required");
        }

        if (existsByReference(reference)) {
            throw new ApiException(
                    "Payment reference has already been processed");
        }

        UserWalletTransaction transaction =
                new UserWalletTransaction();

        transaction.setUserWallets(wallet);
        transaction.setTransactionType("TOP_UP");
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");
        transaction.setReference(reference);

        return userWalletTransactionRepository.save(transaction);
    }
}
