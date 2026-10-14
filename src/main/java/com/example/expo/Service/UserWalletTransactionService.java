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

import java.util.List;

@Service
@AllArgsConstructor
public class UserWalletTransactionService {

    private final UserWalletTransactionRepository userWalletTransactionRepository;
    private final UserWalletsRepository userWalletsRepository;
    private final UserRepository userRepository;

    public List<UserWalletTransaction> getAllTransactions() {
        return userWalletTransactionRepository.findAll();
    }

    public void addTransaction(Integer userId, Integer walletId, UserWalletTransaction transaction) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets wallet = userWalletsRepository.findUserWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (!wallet.getUser().getId().equals(userId)) {
            throw new ApiException("Wallet does not belong to this user");
        }

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new ApiException("Wallet is not active");
        }

        transaction.setUserWallets(wallet);
        userWalletTransactionRepository.save(transaction);
    }

    // Update transaction
    public void updateTransaction(Integer userId, Integer walletId, Integer transactionId, UserWalletTransaction transaction) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets wallet = userWalletsRepository.findUserWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (!wallet.getUser().getId().equals(userId)) {
            throw new ApiException("Wallet does not belong to this user");
        }

        UserWalletTransaction oldTransaction = userWalletTransactionRepository.findUserWalletTransactionById(transactionId);

        if (oldTransaction == null) {
            throw new ApiException("Transaction not found");
        }

        if (!oldTransaction.getUserWallets().getId().equals(walletId)) {
            throw new ApiException(
                    "Transaction does not belong to this wallet");
        }

        if ("SUCCESS".equals(oldTransaction.getStatus())) {
            throw new ApiException(
                    "Successful transactions cannot be updated");
        }

        oldTransaction.setTransactionType(transaction.getTransactionType());
        oldTransaction.setAmount(transaction.getAmount());
        oldTransaction.setStatus(transaction.getStatus());
        oldTransaction.setReference(transaction.getReference());

        userWalletTransactionRepository.save(oldTransaction);
    }

    public void deleteTransaction(Integer userId, Integer walletId, Integer transactionId) {

        User user = userRepository.findUserById(userId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets wallet =
                userWalletsRepository.findUserWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (!wallet.getUser().getId().equals(userId)) {
            throw new ApiException("Wallet does not belong to this user");
        }

        UserWalletTransaction transaction = userWalletTransactionRepository.findUserWalletTransactionById(transactionId);

        if (transaction == null) {
            throw new ApiException("Transaction not found");
        }

        if (!transaction.getUserWallets().getId().equals(walletId)) {
            throw new ApiException(
                    "Transaction does not belong to this wallet");
        }

        if ("SUCCESS".equals(transaction.getStatus())) {
            throw new ApiException(
                    "Successful transactions cannot be deleted");
        }

        userWalletTransactionRepository.delete(transaction);
    }
}
