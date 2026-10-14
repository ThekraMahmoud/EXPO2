package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.SellerWalletResponse;
import com.example.expo.Model.SellerersWallets;
import com.example.expo.Model.Sellerers;
import com.example.expo.Repository.SellersRepository;
import com.example.expo.Repository.SellersWalletsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellersWalletsService {

    private final SellersWalletsRepository sellersWalletsRepository;
    private final SellersRepository sellersRepository;

    //get all
    public List<SellerWalletResponse> getAll() {
        List<SellerersWallets> wallets = sellersWalletsRepository.findAll();
        List<SellerWalletResponse> responses = new ArrayList<>();

        for (int i = 0; i < wallets.size(); i++) {//مشينا ع كل السيلر ولت واضفناها داخل الريسبونس واعدنا فقط القيم الي نحتاجها في الرسيبونس
            responses.add(toResponse(wallets.get(i)));
        }

        return responses;
    }

    //اسندت القيم الي بالمحفظة الى القيم بالرسيبونس
    private SellerWalletResponse toResponse(SellerersWallets wallet) {
        return new SellerWalletResponse(
                wallet.getId(),
                wallet.getSeller().getId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus()
        );
    }

    @Transactional
    public void deleteWallet(Integer walletId) {
        SellerersWallets wallet = sellersWalletsRepository.findSellersWalletsById(walletId);

        if (wallet == null) {
            throw new ApiException("wallet id not found");
        }
        //bigdecimal.zero يقارن حتى بعدد المنازل وليس فقط الرقم
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException("cannot delete wallet with a non-zero balance");
        }
        wallet.getSeller().setSellerersWallets(null);//حذفنا المحفظة من البائع
        sellersWalletsRepository.delete(wallet);
    }


    @Transactional
    public void createWallet(Integer sellerId) {
        Sellerers seller = sellersRepository.findSellerById(sellerId);//البائع موجود
        if(seller==null){
            throw new ApiException("seller id not found");
        }

        if (sellersWalletsRepository.existsBySeller_Id(sellerId)) {//اذا كان عنده بطاقة سابقة ممنوع يسجل من جديد
            throw new ApiException("seller already has a wallet");
        }

        LocalDateTime now = LocalDateTime.now();

        SellerersWallets wallet = new SellerersWallets();//انشاء المحفظة


        wallet.setSeller(seller);//اضفناها للبائع
        wallet.setBalance(BigDecimal.ZERO);//لازم نسند قيمة للبلنس ونبدأ بصفر والتعديل هيكون بالشراء والايداع
        wallet.setCurrency("SAR");
        wallet.setStatus("ACTIVE");
        wallet.setCreated_at(now);
        wallet.setUpdated_at(now);

        sellersWalletsRepository.save(wallet);
        seller.setSellerersWallets(wallet);
    }

    //العملة - وقت الانشاء - الاي دي - الرصيد , ما ينفع نعدلها بالابديت لازم تتعدل لحالها
    @Transactional
    public void updateWallet(Integer walletId, String status) {
        SellerersWallets wallet = sellersWalletsRepository.findSellersWalletsById(walletId);

        if (wallet == null) {//نتأكد ان المحفظة موجودة
            throw new ApiException("wallet id not found");
        }

        if (status == null) {
            throw new ApiException("status is required");
        }

        if (!status.equals("ACTIVE") && !status.equals("SUSPENDED")) {//مجبور يدخل فقط هالخيارين بالسسيتم
            throw new ApiException("status must be ACTIVE or SUSPENDED");
        }

        wallet.setStatus(status);//نعدل حالة المحفظة
        wallet.setUpdated_at(LocalDateTime.now());//نحدث وقت التعديل

        sellersWalletsRepository.save(wallet);
    }







}