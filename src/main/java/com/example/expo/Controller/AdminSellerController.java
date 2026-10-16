package com.example.expo.Controller;
import com.example.expo.Model.Sellerers;
import com.example.expo.Service.AdminSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/admin/restaurants")
public class AdminSellerController {
    private final AdminSellerService adminSellerService;
    @GetMapping("/pending")
    public List<Sellerers> pending(@RequestParam Integer adminId) {
        return adminSellerService.getPendingRestaurants(adminId);
    }
    @PatchMapping("/{sellerId}/verification")
    public Sellerers verify(@RequestParam Integer adminId, @PathVariable Integer sellerId, @RequestParam String status) {
        return adminSellerService.updateVerificationStatus(adminId, sellerId, status);
    }
}
