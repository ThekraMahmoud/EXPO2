
package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.AuthResponse;
import com.example.expo.DTO.LoginRequest;
import com.example.expo.DTO.RegisterUserRequest;
import com.example.expo.DTO.SellerRequest;
import com.example.expo.Model.Sellerers;
import com.example.expo.Model.User;
import com.example.expo.Repository.SellersRepository;
import com.example.expo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SellersRepository sellersRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException("Username is already registered");
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new ApiException("Phone is already registered");
        }

        User user = new User();

        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhone(request.phone());
        user.setPassword(encoder.encode(request.password()));
        user.setRole("USER");
        user.setStatus("ACTIVE");

        userRepository.save(user);

        return new AuthResponse(
                "Registration successful",
                user.getId(),
                user.getRole(),
                user.getStatus()
        );
    }


    public AuthResponse loginUser(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() ->
                        new ApiException("Invalid email or password")
                );

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException("Account is inactive");
        }

        return new AuthResponse(
                "Login successful",
                user.getId(),
                user.getRole(),
                user.getStatus()
        );
    }


    @Transactional
    public AuthResponse registerSeller(SellerRequest request) {

        if (sellersRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered");
        }

        if (sellersRepository.existsByPhone(request.getPhone())) {
            throw new ApiException("Phone is already registered");
        }

        if (sellersRepository.findAll().stream().anyMatch(seller ->
                seller.getCommercial_register() != null
                        && seller.getCommercial_register()
                        .equals(request.getCommercial_register()))) {

            throw new ApiException(
                    "Commercial registration is already registered"
            );
        }

        Sellerers seller = new Sellerers();

        seller.setBusiness_name(request.getBusiness_name().trim());
        seller.setEmail(request.getEmail().trim().toLowerCase());
        seller.setPhone(request.getPhone());
        seller.setPassword(encoder.encode(request.getPassword()));
        seller.setCommercial_register(request.getCommercial_register());

        seller.setVerification_status("PENDING");
        seller.setStatus("ACTIVE");
        seller.setCreated_at(LocalDate.now());
        seller.setUpdated_at(LocalDate.now());

        sellersRepository.save(seller);

        return new AuthResponse(
                "Registration submitted; waiting for admin approval",
                seller.getId(),
                "SELLER",
                seller.getVerification_status()
        );
    }


    public AuthResponse loginSeller(LoginRequest request) {

        Sellerers seller = sellersRepository
                .findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() ->
                        new ApiException("Invalid email or password")
                );

        if (!encoder.matches(request.password(), seller.getPassword())) {
            throw new ApiException("Invalid email or password");
        }

        if (!"ACTIVE".equals(seller.getStatus())) {
            throw new ApiException("Account is inactive");
        }

        if (!"VERIFIED".equals(seller.getVerification_status())) {
            throw new ApiException(
                    "Restaurant is awaiting admin verification"
            );
        }

        return new AuthResponse(
                "Login successful",
                seller.getId(),
                "SELLER",
                seller.getVerification_status()
        );
    }
}
