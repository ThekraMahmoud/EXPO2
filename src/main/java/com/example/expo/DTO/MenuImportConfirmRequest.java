package com.example.expo.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

// يرسل البائع المنتجات بعد مراجعتها؛ الناقصة تُحفظ غير نشطة لإكمالها لاحقًا.
public record MenuImportConfirmRequest(
        @NotNull @Size(min = 1, max = 100) List<@NotNull @Valid MenuImportItemRequest> items) {}
