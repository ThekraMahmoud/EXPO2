package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.SellerWallerTransaction;
import com.example.expo.Model.SellerersWallets;
import com.example.expo.Repository.SellerWallerTransactionRepository;
import com.example.expo.Repository.SellersWalletsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerWallerTransactionService {

    private final SellerWallerTransactionRepository sellerWallerTransactionRepository;
    private final SellersWalletsRepository sellersWalletsRepository;

    //عرض معاملة واحدة
    public SellerWallerTransaction getTransactionById(Integer transactionId) {
        SellerWallerTransaction transaction =
                sellerWallerTransactionRepository.findSellerWallerTransactionById(transactionId);

        if (transaction == null) {
            throw new ApiException("transaction id not found");
        }

        return transaction;
    }

    //عرض معاملات محفظة معينة
    public List<SellerWallerTransaction> getTransactionsByWalletId(Integer walletId) {
        SellerersWallets wallet =
                sellersWalletsRepository.findSellersWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("wallet id not found");
        }

        return sellerWallerTransactionRepository.findAllBySellerWallet_Id(walletId);
    }



}