package com.example.expo.Controller;

import com.example.expo.Client.MoyasarClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MoyasarClient moyasarClient;

    @GetMapping("/verify/{paymentId}")
    public String verifyPayment(@PathVariable String paymentId) {
        return moyasarClient.verifyPayment(paymentId);
    }
}