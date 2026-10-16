package com.example.expo.Controller;

import com.example.expo.Model.Payment;
import com.example.expo.DTO.PaymentRecordResponse;
import com.example.expo.Service.PaymentService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// إضافة القراءة فقط؛ مسار Moyasar القديم ومنطق الدفع لا يتغيران.
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/payments")
public class PaymentReadController {
    private final PaymentService paymentService;
    @GetMapping("/all") public List<PaymentRecordResponse> getAllPayments() { return paymentService.getAllPayments().stream().map(PaymentRecordResponse::from).toList(); }
    @GetMapping("/{paymentId}") public PaymentRecordResponse getPaymentById(@PathVariable @Positive Integer paymentId) {
        return PaymentRecordResponse.from(paymentService.getPaymentById(paymentId));
    }
    @GetMapping("/order/{orderId}") public List<PaymentRecordResponse> getPaymentsByOrder(@PathVariable @Positive Integer orderId) {
        return paymentService.getPaymentsByOrder(orderId).stream().map(PaymentRecordResponse::from).toList();
    }
    @GetMapping("/status/{status}") public List<PaymentRecordResponse> getPaymentsByStatus(@PathVariable @NotBlank String status) {
        return paymentService.getPaymentsByStatus(status).stream().map(PaymentRecordResponse::from).toList();
    }
}
