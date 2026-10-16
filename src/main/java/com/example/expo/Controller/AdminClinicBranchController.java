package com.example.expo.Controller;

import com.example.expo.Model.ClinicBranch;
import com.example.expo.Service.AdminSellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// كنترولر مستقل حتى لا نغير مسارات اعتماد المؤسسات الموجودة.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/clinic-branches")
public class AdminClinicBranchController {
    private final AdminSellerService adminSellerService;
    @GetMapping
    public List<ClinicBranch> getAllClinicBranches(@RequestParam Integer adminId) {
        return adminSellerService.getAllClinicBranches(adminId);
    }
    @GetMapping("/{id}")
    public ClinicBranch getClinicBranchById(@RequestParam Integer adminId, @PathVariable Integer id) {
        return adminSellerService.getClinicBranchById(adminId, id);
    }
    @PostMapping
    public ClinicBranch addClinicBranch(@RequestParam Integer adminId, @Valid @RequestBody ClinicBranch branch) {
        return adminSellerService.addClinicBranch(adminId, branch);
    }
    @PutMapping("/{id}")
    public ClinicBranch updateClinicBranch(@RequestParam Integer adminId, @PathVariable Integer id,
                                           @Valid @RequestBody ClinicBranch branch) {
        return adminSellerService.updateClinicBranch(adminId, id, branch);
    }
    @DeleteMapping("/{id}")
    public void deleteClinicBranch(@RequestParam Integer adminId, @PathVariable Integer id) {
        adminSellerService.deleteClinicBranch(adminId, id);
    }
}
