package com.example.expo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParamedicRequestInput(
    @NotBlank(message = "يرجى كتابة موقعك بالتفصيل")
    @Size(max = 500) String locationDescription,
    @Size(max = 1000) String note
) {}
