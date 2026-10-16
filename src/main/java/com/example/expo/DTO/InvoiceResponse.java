package com.example.expo.DTO;

import com.example.expo.Model.Invoices;
import com.example.expo.Model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(Integer invoiceId, String invoiceNumber, Integer orderId,
                              String sellerName, String description, String status,
                              LocalDateTime createdAt, LocalDateTime paidAt,
                              LocalDateTime expiresAt, String qrToken, List<Item> items,
                              BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
    public record Item(String name, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {}

    public static InvoiceResponse from(Invoices invoice) {
        Order order = invoice.getOrder();
        List<Item> items = order == null ? List.of() : order.getItems().stream()
                .map(item -> new Item(item.getItemName(), item.getQuantity(), item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();
        return new InvoiceResponse(invoice.getId(), invoice.getInvoice_number(),
                order == null ? null : order.getId(), invoice.getSeller().getBusiness_name(),
                invoice.getDescription(), invoice.getStatus(), invoice.getCreated_at(),
                invoice.getPaid_at(), invoice.getExpires_at(), invoice.getQr_token(), items,
                invoice.getSubtotal(), invoice.getTax_amount(), invoice.getTotal_amount());
    }
}
