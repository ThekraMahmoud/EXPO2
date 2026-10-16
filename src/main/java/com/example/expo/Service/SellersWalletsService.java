        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Sellerers;
import com.example.expo.Model.SellerersWallets;
import com.example.expo.Model.SellerWallerTransaction;
import com.example.expo.Repository.SellersRepository;
import com.example.expo.Repository.SellersWalletsRepository;
import com.example.expo.Repository.SellerWallerTransactionRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SellersWalletsService {

    private final SellersWalletsRepository sellersWalletsRepository;
    private final SellersRepository sellersRepository;
    private final SellerWallerTransactionRepository
            sellerWallerTransactionRepository;


    // عرض جميع المحافظ
    public List<SellerersWallets> getAllWallets() {
        return sellersWalletsRepository.findAll();
    }


    // عرض محفظة بائع محدد
    public SellerersWallets getWalletBySellerId(Integer sellerId) {

        Sellerers seller = sellersRepository.findSellerById(sellerId);

        if (seller == null) {
            throw new ApiException("Seller not found");
        }

        return sellersWalletsRepository.findAll()
                .stream()
                .filter(wallet -> wallet.getSeller() != null
                        && wallet.getSeller().getId().equals(sellerId))
                .findFirst()
                .orElseThrow(() ->
                        new ApiException("Seller wallet not found"));
    }


    // إنشاء محفظة للبائع
    @Transactional
    public SellerersWallets addWallet(Integer sellerId) {

        Sellerers seller = sellersRepository.findSellerById(sellerId);

        if (seller == null) {
            throw new ApiException("Seller not found");
        }

        if (!"VERIFIED".equals(seller.getVerification_status()) || !"ACTIVE".equals(seller.getStatus()))
            throw new ApiException("Seller must be approved and active before creating a wallet");
        boolean walletExists = sellersWalletsRepository.findAll()
                .stream()
                .anyMatch(wallet -> wallet.getSeller() != null
                        && wallet.getSeller().getId().equals(sellerId));

        if (walletExists) {
            throw new ApiException(
                    "Seller already has a wallet");
        }


        SellerersWallets wallet = new SellerersWallets();

        wallet.setSeller(seller);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus("ACTIVE"); wallet.setUpdated_at(LocalDateTime.now());

// إضافة التاريخين
        wallet.setCreated_at(LocalDateTime.now());
        wallet.setUpdated_at(LocalDateTime.now());

// حفظ المحفظة
        return sellersWalletsRepository.save(wallet);

    }

    // إيقاف المحفظة
    @Transactional
    public void suspendWallet(Integer walletId) {

        SellerersWallets wallet =
                sellersWalletsRepository.findByIdForUpdate(walletId).orElse(null);

        if (wallet == null) {
            throw new ApiException("Seller wallet not found");
        }

        wallet.setStatus("SUSPENDED"); wallet.setUpdated_at(LocalDateTime.now());

        sellersWalletsRepository.save(wallet);
    }


    // تفعيل المحفظة
    @Transactional
    public void activateWallet(Integer walletId) {

        SellerersWallets wallet =
                sellersWalletsRepository.findByIdForUpdate(walletId).orElse(null);

        if (wallet == null) {
            throw new ApiException("Seller wallet not found");
        }

        wallet.setStatus("ACTIVE"); wallet.setUpdated_at(LocalDateTime.now());

        sellersWalletsRepository.save(wallet);
    }


    // طلب تحويل مبلغ من محفظة البائع إلى حسابه البنكي
    @Transactional
    public SellerWallerTransaction requestBankTransfer(
            Integer walletId,
            BigDecimal amount) {

        SellerersWallets wallet =
                sellersWalletsRepository.findByIdForUpdate(walletId).orElse(null);

        if (wallet == null) {
            throw new ApiException("Seller wallet not found");
        }

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new ApiException("Seller wallet is not active");
        }

        if (amount == null
                || amount.signum() <= 0
                || amount.scale() > 2) {
            throw new ApiException("Invalid transfer amount");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient wallet balance");
        }

        // خصم المبلغ وحجزه لطلب التحويل
        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        sellersWalletsRepository.save(wallet);

        wallet.setUpdated_at(LocalDateTime.now());
        // إنشاء سجل التحويل بحالة انتظار
        SellerWallerTransaction transaction =
                new SellerWallerTransaction();

        transaction.setSellerWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType("BANK_TRANSFER");
        transaction.setStatus("PENDING");
        transaction.setReference(
                "BANK-" + UUID.randomUUID()
        );
        transaction.setCreated_at(LocalDateTime.now());

        return sellerWallerTransactionRepository.save(transaction);
    }


    // عرض معاملات محفظة البائع
    public List<SellerWallerTransaction> getWalletTransactions(
            Integer walletId) {

        SellerersWallets wallet =
                sellersWalletsRepository.findSellersWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("Seller wallet not found");
        }

        return sellerWallerTransactionRepository
                .findAllBySellerWallet_Id(walletId);
    }



}