package com.example.expo.DTO;
public record PaymentConfirmationResponse(String message, Integer orderId, String paymentStatus, String status, Integer invoiceId) {
    public PaymentConfirmationResponse(String message, Integer orderId, String paymentStatus, String status) {
        this(message, orderId, paymentStatus, status, null);
    }
}
