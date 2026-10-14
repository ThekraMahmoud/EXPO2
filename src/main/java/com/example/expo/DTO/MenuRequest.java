package com.example.expo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuRequest(@NotBlank @Size(max=150) String name) {}
