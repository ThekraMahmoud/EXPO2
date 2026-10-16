package com.example.expo.Controller;

import com.example.expo.DTO.CheckoutRequest;
import com.example.expo.DTO.OrderStatusRequest;
import com.example.expo.DTO.OrderResponse;
import com.example.expo.DTO.PaymentConfirmationResponse;
import com.example.expo.Model.Invoices;
import com.example.expo.Service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(orderService.getAllOrders().stream().map(o -> OrderResponse.from(o, "Orders retrieved successfully")).toList());
    }

    @PostMapping("/user/{userId}/checkout/{cartId}")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Integer userId,
            @PathVariable Integer cartId,
            @Valid @RequestBody CheckoutRequest request) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.checkout(userId, cartId, request),
                        "Order created successfully"
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> customerOrders(
            @PathVariable Integer userId) {

        return ResponseEntity.ok(
                orderService.customerOrders(userId).stream()
                        .map(order -> OrderResponse.from(
                                order,
                                "Orders retrieved successfully"
                        ))
                        .toList()
        );
    }

    @GetMapping("/{orderId}/user/{userId}")
    public ResponseEntity<OrderResponse> getCustomerOrder(
            @PathVariable Integer userId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.getCustomerOrder(userId, orderId),
                        "Order retrieved successfully"
                )
        );
    }

    @GetMapping("/restaurant/{sellerId}")
    public ResponseEntity<List<OrderResponse>> sellerOrders(
            @PathVariable Integer sellerId) {

        return ResponseEntity.ok(
                orderService.sellerOrders(sellerId).stream()
                        .map(order -> OrderResponse.from(
                                order,
                                "Orders retrieved successfully"
                        ))
                        .toList()
        );
    }

    @PatchMapping("/{orderId}/restaurant/{sellerId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId,
            @Valid @RequestBody OrderStatusRequest request) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.updateSellerStatus(
                                sellerId,
                                orderId,
                                request.status()
                        ),
                        "Order status updated successfully"
                )
        );
    }

    @PatchMapping("/{orderId}/restaurant/{sellerId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.updateSellerStatus(
                                sellerId,
                                orderId,
                                "ACCEPTED"
                        ),
                        "Order accepted successfully"
                )
        );
    }

    @PatchMapping("/{orderId}/restaurant/{sellerId}/prepare")
    public ResponseEntity<OrderResponse> prepareOrder(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.updateSellerStatus(
                                sellerId,
                                orderId,
                                "PREPARING"
                        ),
                        "Order preparation started successfully"
                )
        );
    }

    @PatchMapping("/{orderId}/restaurant/{sellerId}/ready")
    public ResponseEntity<OrderResponse> readyOrder(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.updateSellerStatus(
                                sellerId,
                                orderId,
                                "READY"
                        ),
                        "Order is ready for pickup"
                )
        );
    }

    @PatchMapping("/{orderId}/restaurant/{sellerId}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                OrderResponse.from(
                        orderService.updateSellerStatus(
                                sellerId,
                                orderId,
                                "COMPLETED"
                        ),
                        "Order completed successfully"
                )
        );
    }

    @PostMapping("/{orderId}/restaurant/{sellerId}/payments/on-site/confirm")
    public ResponseEntity<PaymentConfirmationResponse> confirmOnSitePayment(
            @PathVariable Integer sellerId,
            @PathVariable Integer orderId) {

        return ResponseEntity.ok(
                orderService.confirmOnSitePayment(sellerId, orderId)
        );
    }

    @PostMapping("/{orderId}/user/{userId}/payments/moyasar/{paymentId}/confirm")
    public ResponseEntity<Invoices> confirmPayment(
            @PathVariable Integer userId,
            @PathVariable Integer orderId,
            @PathVariable @NotBlank String paymentId) {

        return ResponseEntity.ok(
                orderService.confirmMoyasarPayment(
                        userId,
                        orderId,
                        paymentId
                )
        );
    }
}