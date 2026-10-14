package com.example.expo.DTO;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceRequest {

    @NotBlank
    @Size(max = 50)
    private String invoice_number;

    @Positive
    private Integer user_id;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    private BigDecimal subtotal;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    private BigDecimal tax_amount;

    @NotBlank
    @Size(max = 255)
    private String qr_token;

    @Future
    private LocalDateTime expires_at;
}