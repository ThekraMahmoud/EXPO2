package com.example.expo.Controller;

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
    @GetMapping("/user/{userId}") public List<Cart> getUserCarts(@PathVariable Integer userId) { return cartService.userCarts(userId); }
    @PostMapping("/user/{userId}/restaurant/{sellerId}/items") public Cart addItem(@PathVariable Integer userId, @PathVariable Integer sellerId, @Valid @RequestBody CartItemRequest request) { return cartService.addItem(userId, sellerId, request); }
    @PutMapping("/{cartId}/user/{userId}/items/{itemId}") public Cart updateQuantity(@PathVariable Integer userId, @PathVariable Integer cartId, @PathVariable Integer itemId, @RequestParam @Min(1) Integer quantity) { return cartService.updateQuantity(userId, cartId, itemId, quantity); }
    @DeleteMapping("/{cartId}/user/{userId}/items/{itemId}") public Cart removeItem(@PathVariable Integer userId, @PathVariable Integer cartId, @PathVariable Integer itemId) { return cartService.removeItem(userId, cartId, itemId); }
    @DeleteMapping("/{cartId}/user/{userId}/items") public void clear(@PathVariable Integer userId, @PathVariable Integer cartId) { cartService.clear(userId, cartId); }
}
