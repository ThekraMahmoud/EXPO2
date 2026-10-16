package com.example.expo.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record MenuItemRequest(@NotBlank @Size(max=100) String name, @Size(max=500) String description,
                              @NotNull @DecimalMin("0.0") @Digits(integer=8, fraction=2) BigDecimal price, Boolean available) {}
