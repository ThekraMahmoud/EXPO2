package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.DTO.SellerWalletResponse;
import com.example.expo.Service.SellersWalletsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/seller-wallet")
public class SellersWalletsController {

    private final SellersWalletsService sellersWalletsService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllWallet(){
        List<SellerWalletResponse> walletList= sellersWalletsService.getAll();
        return ResponseEntity.status(200).body(walletList);
    }
    @PostMapping("/add/{sellerId}")
    public ResponseEntity<?>addWallet(@PathVariable Integer sellerId){
        sellersWalletsService.createWallet(sellerId);
        return ResponseEntity.status(200).body(new ApiResponse("added done"));
    }

    @PutMapping("/update/{walletId}")
    public ResponseEntity<?>updateWallet(@PathVariable Integer walletId,@RequestParam String status){
        sellersWalletsService.updateWallet(walletId,status);
        return ResponseEntity.status(200).body(new ApiResponse("updated done"));
    }
    @DeleteMapping("/delete/{walletId}")
    public ResponseEntity<?>deleteWallet(@PathVariable Integer walletId){
        sellersWalletsService.deleteWallet(walletId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted done"));
    }


}