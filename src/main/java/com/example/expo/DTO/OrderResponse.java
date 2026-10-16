package com.example.expo.DTO;

import com.example.expo.Model.Order;
import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(String message, Integer orderId, String sellerName,
                            String status, String paymentMethod, String paymentStatus,
                            BigDecimal totalAmount, List<Item> items) {
    public record Item(String itemName, Integer quantity, BigDecimal unitPrice) {}

    public static OrderResponse from(Order order, String message) {
        return new OrderResponse(message, order.getId(), order.getSeller().getBusiness_name(),
                order.getStatus(), order.getPaymentMethod(), order.getPaymentStatus(),
                order.getTotalAmount(), order.getItems().stream()
                .map(item -> new Item(item.getItemName(), item.getQuantity(), item.getUnitPrice()))
                .toList());
    }
}
