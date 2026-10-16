package com.example.expo.DTO;
import java.math.BigDecimal;
import java.util.List;
public record PaymentResponse(Integer paymentId, String storeName, List<ItemOutput> items, BigDecimal totalAmount,
                              String status, Integer orderId, String message) {
    public record ItemOutput(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
    public PaymentResponse(Integer paymentId, String storeName, List<ItemOutput> items, BigDecimal totalAmount, String status) {
        this(paymentId, storeName, items, totalAmount, status, null, null);
    }
}
