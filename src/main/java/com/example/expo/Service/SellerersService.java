package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.SellerRequest;
import com.example.expo.Model.Sellerers;
import com.example.expo.Repository.InvoicesRepository;
import com.example.expo.Repository.SellersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerersService {
    private final SellersRepository sellersRepository;
    private final InvoicesRepository invoicesRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public List<Sellerers> getAll()
    {
        return sellersRepository.findAll();
    }


    public void addSeller(SellerRequest request) {
        Sellerers seller = new Sellerers();

        seller.setBusiness_name(request.getBusiness_name());
        seller.setEmail(request.getEmail());
        seller.setPhone(request.getPhone());
        seller.setPassword(passwordEncoder.encode(request.getPassword()));
        seller.setCommercial_register(request.getCommercial_register());

        // قيم يحددها النظام.
        seller.setVerification_status("PENDING");
        seller.setStatus("ACTIVE");

        LocalDate today = LocalDate.now();
        seller.setCreated_at(today);
        seller.setUpdated_at(today);
        sellersRepository.save(seller);


    }

    public void updateSeller(Integer sellerId, SellerRequest request) {
        Sellerers oldSeller =
                sellersRepository.findSellerById(sellerId);

        if (oldSeller == null) {
            throw new ApiException("seller id not found");
        }

        oldSeller.setBusiness_name(request.getBusiness_name());
        oldSeller.setEmail(request.getEmail());
        oldSeller.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) oldSeller.setPassword(passwordEncoder.encode(request.getPassword()));
        oldSeller.setCommercial_register(request.getCommercial_register());

        // تغيير بيانات التسجيل يلغي التوثيق السابق.
        if ("VERIFIED".equals(oldSeller.getVerification_status())) {
            oldSeller.setVerification_status("PENDING");
        }

        // لا نغيّر created_at أو status من هذا الطلب.
        oldSeller.setUpdated_at(LocalDate.now());

        sellersRepository.save(oldSeller);
    }

    public void deleteSeller(Integer sellerId){
        Sellerers oldSeller= sellersRepository.findSellerById(sellerId);

        if(oldSeller==null){
            throw new ApiException("seller id not found");
        }
        if(invoicesRepository.existsBySeller_Id(sellerId)){
            throw new ApiException("can not delete seller with invoices");
        }
        sellersRepository.delete(oldSeller);
    }



}