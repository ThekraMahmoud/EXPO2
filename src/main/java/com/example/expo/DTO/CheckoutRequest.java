package com.example.expo.DTO;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(@NotBlank String paymentMethod) {}
