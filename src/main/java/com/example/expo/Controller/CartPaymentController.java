package com.example.expo.Controller;
import com.example.expo.DTO.PaymentResponse;
import com.example.expo.Service.PaymentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor @Validated @RequestMapping("/api/v1/payments")
public class CartPaymentController {
    private final PaymentService paymentService;
    @PostMapping("/cart/{cartId}/user/{userId}")
    public PaymentResponse payCart(@PathVariable @Positive Integer cartId, @PathVariable @Positive Integer userId) {
        return paymentService.payCart(userId, cartId);
    }
}
