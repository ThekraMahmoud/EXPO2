package com.example.expo.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(@NotNull @Min(1) Integer menuItemId, @NotNull @Min(1) Integer quantity) {}
