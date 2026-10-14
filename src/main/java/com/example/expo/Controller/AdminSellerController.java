package com.example.expo.Controller;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Sellerers;
import com.example.expo.Repository.SellersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/admin/restaurants")
public class AdminSellerController {
    private final SellersRepository sellersRepository;
    @GetMapping("/pending") public List<Sellerers> pending() { return sellersRepository.findAll().stream().filter(s -> "PENDING".equals(s.getVerification_status())).toList(); }
    @PatchMapping("/{sellerId}/verification") @Transactional public Sellerers verify(@PathVariable Integer sellerId, @RequestParam String status) {
        String next = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("VERIFIED", "REJECTED", "PENDING").contains(next)) throw new ApiException("Status must be VERIFIED, REJECTED, or PENDING");
        Sellerers s = sellersRepository.findById(sellerId).orElseThrow(() -> new ApiException("Restaurant not found"));
        s.setVerification_status(next); s.setUpdated_at(LocalDate.now()); return sellersRepository.save(s);
    }
}
