package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Model.UserWallets;
import com.example.expo.Repository.UserRepository;
import com.example.expo.Repository.UserWalletsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserWalletsService {

    private final UserWalletsRepository userWalletsRepository;
    private final UserRepository userRepository;

    // Get all wallets
    public List<UserWallets> getAllWallets() {
        return userWalletsRepository.findAll();
    }

    // Add wallet
    public void addWallet(Integer userId, UserWallets wallet) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets oldWallet = userWalletsRepository.findUserWalletsByUserId(userId);
        if (oldWallet != null) {
            throw new ApiException("User already has a wallet");
        }

        wallet.setUser(user);
        userWalletsRepository.save(wallet);
    }

    public void updateWallet(Integer userId, Integer walletId, UserWallets wallet) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        UserWallets oldWallet = userWalletsRepository.findUserWalletsById(walletId);

        if (oldWallet == null) {
            throw new ApiException("Wallet not found");
        }

        if (!oldWallet.getUser().getId().equals(userId)) {
            throw new ApiException("Wallet does not belong to this user");
        }

        oldWallet.setStatus(wallet.getStatus());
        userWalletsRepository.save(oldWallet);
    }

    public void deleteWallet(Integer userId, Integer walletId) {
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
        userWalletsRepository.delete(wallet);
    }
}
