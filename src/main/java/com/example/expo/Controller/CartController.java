package com.example.expo.Controller;
import com.example.expo.DTO.CartResponse;

import com.example.expo.DTO.CartItemRequest;
import com.example.expo.Model.Cart;
import com.example.expo.Service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartService cartService;
    @GetMapping("/user/{userId}") public List<CartResponse> getUserCarts(@PathVariable Integer userId) { return cartService.userCartOutputs(userId); }
    @PostMapping("/user/{userId}/restaurant/{sellerId}/items") public CartResponse addItem(@PathVariable Integer userId, @PathVariable Integer sellerId, @Valid @RequestBody CartItemRequest request) { return cartService.addItemOutput(userId, sellerId, request); }
    @PutMapping("/{cartId}/user/{userId}/items/{itemId}") public CartResponse updateQuantity(@PathVariable Integer userId, @PathVariable Integer cartId, @PathVariable Integer itemId, @RequestParam @Min(1) Integer quantity) { return cartService.updateQuantityOutput(userId, cartId, itemId, quantity); }
    @DeleteMapping("/{cartId}/user/{userId}/items/{itemId}") public CartResponse removeItem(@PathVariable Integer userId, @PathVariable Integer cartId, @PathVariable Integer itemId) { return cartService.removeItemOutput(userId, cartId, itemId); }
    @DeleteMapping("/{cartId}/user/{userId}/items") public CartResponse clear(@PathVariable Integer userId, @PathVariable Integer cartId) { return cartService.clearOutput(userId, cartId); }

    @GetMapping("/{cartId}/user/{userId}")
    public CartResponse getCartById(@PathVariable Integer cartId, @PathVariable Integer userId) {
        return cartService.getCartById(userId, cartId);
    }

}