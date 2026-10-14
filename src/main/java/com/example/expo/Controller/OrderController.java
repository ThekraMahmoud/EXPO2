package com.example.expo.Controller;

import com.example.expo.DTO.CheckoutRequest;
import com.example.expo.DTO.OrderStatusRequest;
import com.example.expo.Model.Order;
import com.example.expo.Model.Invoices;
import com.example.expo.Service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    @PostMapping("/user/{userId}/checkout/{cartId}")
    public Order checkout(@PathVariable Integer userId, @PathVariable Integer cartId, @Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(userId, cartId, request); }
    @GetMapping("/user/{userId}")
    public List<Order> customerOrders(@PathVariable Integer userId)
    {
        return orderService.customerOrders(userId); }
    @GetMapping("/{orderId}/user/{userId}")
    public Order getCustomerOrder(@PathVariable Integer userId, @PathVariable Integer orderId)
    { return orderService.getCustomerOrder(userId, orderId); }

    @GetMapping("/restaurant/{sellerId}")
    public List<Order> sellerOrders(@PathVariable Integer sellerId) {
        return orderService.sellerOrders(sellerId); }
    @PatchMapping("/{orderId}/restaurant/{sellerId}/status")
    public Order updateStatus(@PathVariable Integer sellerId, @PathVariable Integer orderId, @Valid @RequestBody OrderStatusRequest request) {
        return orderService.updateSellerStatus(sellerId, orderId, request.status()); }
    @PostMapping("/{orderId}/restaurant/{sellerId}/payments/on-site/confirm")
    public Invoices confirmOnSitePayment(@PathVariable Integer sellerId, @PathVariable Integer orderId)
    { return orderService.confirmOnSitePayment(sellerId, orderId); }
        @PostMapping("/{orderId}/user/{userId}/payments/moyasar/{paymentId}/confirm")
        public Invoices confirmPayment(@PathVariable Integer userId, @PathVariable Integer orderId, @PathVariable @NotBlank String paymentId) { return orderService.confirmMoyasarPayment(userId, orderId, paymentId); }
}
