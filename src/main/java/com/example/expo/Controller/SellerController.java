package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.DTO.SellerRequest;
import com.example.expo.Model.Sellerers;
import com.example.expo.Service.SellerersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerersService sellerersService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllSeller(){
        List<Sellerers> sellerList= sellerersService.getAll();
        return ResponseEntity.status(200).body(sellerList);
    }
    @PostMapping("/add")
    public ResponseEntity<?>addSeller(@RequestBody @Valid SellerRequest seller){
        sellerersService.addSeller(seller);
        return ResponseEntity.status(200).body(new ApiResponse("added done"));
    }

    @PutMapping("/update/{sellerId}")
    public ResponseEntity<?>updateSeller(@PathVariable Integer sellerId,@RequestBody @Valid SellerRequest seller){
        sellerersService.updateSeller(sellerId,seller);
        return ResponseEntity.status(200).body(new ApiResponse("updated done"));
    }
    @DeleteMapping("/delete/{sellerId}")
    public ResponseEntity<?>deleteSeller(@PathVariable Integer sellerId){
        sellerersService.deleteSeller(sellerId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted done"));
    }


}