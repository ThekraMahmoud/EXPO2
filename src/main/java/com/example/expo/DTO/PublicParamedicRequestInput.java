package com.example.expo.DTO;

import jakarta.validation.constraints.*;

/** Contact details belong to the person reporting, not to the card owner. */
public record PublicParamedicRequestInput(
    @NotBlank(message = "يرجى كتابة اسم مقدم البلاغ")
    @Size(max = 100, message = "الاسم يجب ألا يتجاوز 100 حرف") String reporterName,
    @NotBlank(message = "يرجى كتابة رقم جوال مقدم البلاغ")
    @Pattern(regexp = "^\\+?[0-9٠-٩][0-9٠-٩ ()-]{5,27}[0-9٠-٩]$", message = "يرجى كتابة رقم جوال صحيح")
    String reporterPhone,
    @NotBlank(message = "يرجى كتابة موقع المريض بالتفصيل")
    @Size(max = 500, message = "الموقع يجب ألا يتجاوز 500 حرف") String locationDescription,
    @Size(max = 1000, message = "الملاحظة يجب ألا تتجاوز 1000 حرف") String note
) {}
