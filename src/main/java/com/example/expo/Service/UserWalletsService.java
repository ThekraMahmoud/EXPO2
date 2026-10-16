
package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserWallets;
import com.example.expo.Repository.UserRepository;
import com.example.expo.Repository.UserWalletsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class UserWalletsService {

    private final UserWalletsRepository userWalletsRepository;
    private final UserRepository userRepository;
    private final UserWalletTransactionService userWalletTransactionService;

    // Get all wallets
    public List<UserWallets> getAllWallets() {
        return userWalletsRepository.findAll();
    }

    // Create one wallet per user
    @Transactional
    public void addWallet(Integer userId) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        if (userWalletsRepository.findUserWalletsByUserId(userId) != null) {
            throw new ApiException("User already has a wallet");
        }

        UserWallets wallet = new UserWallets();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus("ACTIVE");

        userWalletsRepository.save(wallet);
    }

    // Suspend wallet
    @Transactional
    public void suspendWallet(Integer userId, Integer walletId) {

        UserWallets wallet = getOwnedWallet(userId, walletId);

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new ApiException("Wallet is not active");
        }

        wallet.setStatus("SUSPENDED");
        userWalletsRepository.save(wallet);
    }

    // Activate wallet
    @Transactional
    public void activateWallet(Integer userId, Integer walletId) {

        UserWallets wallet = getOwnedWallet(userId, walletId);

        if (!"SUSPENDED".equals(wallet.getStatus())) {
            throw new ApiException("Wallet is not suspended");
        }

        wallet.setStatus("ACTIVE");
        userWalletsRepository.save(wallet);
    }

    // Validate wallet ownership
    private UserWallets getOwnedWallet(Integer userId, Integer walletId) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets wallet =
                userWalletsRepository.findByIdForUpdate(walletId).orElse(null);

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (wallet.getUser() == null
                || !wallet.getUser().getId().equals(userId)) {
            throw new ApiException("Wallet does not belong to this user");
        }

        return wallet;
    }

    // Credit wallet after trusted payment confirmation
    @Transactional
    public void creditWalletAfterVerifiedTopUp(
            Integer userId,
            Integer walletId,
            BigDecimal amount,
            String verifiedPaymentReference) {

        UserWallets wallet = getOwnedWallet(userId, walletId);

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new ApiException("Wallet is not active");
        }

        if (amount == null || amount.signum() <= 0
                || amount.scale() > 2) {
            throw new ApiException("Invalid top-up amount");
        }

        if (verifiedPaymentReference == null
                || verifiedPaymentReference.isBlank()) {
            throw new ApiException("Payment reference is required");
        }

        if (userWalletTransactionService
                .existsByReference(verifiedPaymentReference)) {
            throw new ApiException(
                    "Payment reference has already been processed");
        }

        BigDecimal currentBalance = wallet.getBalance();

        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        wallet.setBalance(currentBalance.add(amount));
        userWalletsRepository.save(wallet);

        userWalletTransactionService.saveVerifiedTopUp(
                wallet,
                amount,
                verifiedPaymentReference
        );
    }
}
