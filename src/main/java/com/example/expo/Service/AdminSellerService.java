
package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.ClinicBranch;
import com.example.expo.Model.Sellerers;
import com.example.expo.Model.User;
import com.example.expo.Repository.ClinicBranchRepository;
import com.example.expo.Repository.SellersRepository;
import com.example.expo.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private final ClinicBranchRepository clinicBranchRepository;
    private final SellersRepository sellersRepository;
    private final UserRepository userRepository;

    // Validate admin before any admin operation
    private User validateAdmin(Integer adminId) {

        if (adminId == null) {
            throw new ApiException("Admin ID is required");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ApiException("Admin not found"));

        if (!"ADMIN".equals(admin.getRole())
                || !"ACTIVE".equals(admin.getStatus())) {
            throw new ApiException(
                    "Only active admins can perform this operation");
        }

        return admin;
    }

    // ==================== Restaurants ====================

    // Get pending restaurants
    public List<Sellerers> getPendingRestaurants(Integer adminId) {

        validateAdmin(adminId);

        return sellersRepository.findAll()
                .stream()
                .filter(s -> "PENDING".equalsIgnoreCase(
                        s.getVerification_status()))
                .toList();
    }

    // Verify, reject, or return restaurant to pending
    @Transactional
    public Sellerers updateVerificationStatus(
            Integer adminId,
            Integer sellerId,
            String status) {

        validateAdmin(adminId);

        if (status == null || status.isBlank()) {
            throw new ApiException("Status is required");
        }

        String nextStatus = status.trim().toUpperCase(Locale.ROOT);

        if (!List.of("VERIFIED", "REJECTED", "PENDING")
                .contains(nextStatus)) {
            throw new ApiException(
                    "Status must be VERIFIED, REJECTED, or PENDING");
        }

        Sellerers seller = sellersRepository.findById(sellerId)
                .orElseThrow(() ->
                        new ApiException("Restaurant not found"));

        seller.setVerification_status(nextStatus);
        seller.setUpdated_at(LocalDate.now());

        return sellersRepository.save(seller);
    }

    // ==================== Clinic Branches ====================

    // Get all clinic branches
    public List<ClinicBranch> getAllClinicBranches(Integer adminId) {

        validateAdmin(adminId);

        return clinicBranchRepository.findAll();
    }

    // Get clinic branch by ID
    public ClinicBranch getClinicBranchById(
            Integer adminId,
            Integer id) {

        validateAdmin(adminId);

        return clinicBranchRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Clinic branch not found"));
    }

    // Add clinic branch
    public ClinicBranch addClinicBranch(
            Integer adminId,
            ClinicBranch branch) {
        validateAdmin(adminId);

        return clinicBranchRepository.save(branch);
    }

    // Update clinic branch
    @Transactional
    public ClinicBranch updateClinicBranch(
            Integer adminId,
            Integer id,
            ClinicBranch updatedBranch) {

        validateAdmin(adminId);

        ClinicBranch branch = clinicBranchRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Clinic branch not found"));

        branch.setName(updatedBranch.getName());
        branch.setZone(updatedBranch.getZone());
        branch.setLocationDescription(
                updatedBranch.getLocationDescription());
        branch.setPhone(updatedBranch.getPhone());
        branch.setEmail(updatedBranch.getEmail());
        branch.setOpeningHours(updatedBranch.getOpeningHours());
        branch.setServicesDescription(
                updatedBranch.getServicesDescription());

        return clinicBranchRepository.save(branch);
    }

    // Delete clinic branch
    @Transactional
    public void deleteClinicBranch(
            Integer adminId,
            Integer id) {

        validateAdmin(adminId);

        ClinicBranch branch = clinicBranchRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException("Clinic branch not found"));

        clinicBranchRepository.delete(branch);
    }
}
