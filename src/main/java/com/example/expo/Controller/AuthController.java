package com.example.expo.Controller;

import com.example.expo.DTO.*;
import com.example.expo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register/user") public AuthResponse registerUser(@Valid @RequestBody RegisterUserRequest request) { return authService.registerUser(request); }
    @PostMapping("/login/user") public AuthResponse loginUser(@Valid @RequestBody LoginRequest request) { return authService.loginUser(request); }
    @PostMapping("/register/restaurant") public AuthResponse registerSeller(@Valid @RequestBody SellerRequest request) { return authService.registerSeller(request); }
    @PostMapping("/login/restaurant") public AuthResponse loginSeller(@Valid @RequestBody LoginRequest request) { return authService.loginSeller(request); }
}
