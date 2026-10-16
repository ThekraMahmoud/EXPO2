package com.example.expo.DTO;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

// الاستيراد يقبل الحقول الناقصة للمراجعة؛ القيود تمنع القيم غير الصالحة فقط.
public record MenuImportItemRequest(@Size(max=100) String name,
        @Size(max=500) String description,
        @DecimalMin("0.0") @Digits(integer=8, fraction=2) BigDecimal price,
        Boolean available) {}
