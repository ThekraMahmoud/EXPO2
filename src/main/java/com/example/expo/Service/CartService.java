package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.CartItemRequest;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final SellersRepository sellersRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public Cart addItem(Integer userId, Integer sellerId, CartItemRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        Sellerers seller = sellersRepository.findById(sellerId).orElseThrow(() -> new ApiException("Restaurant not found"));
        if (!"VERIFIED".equals(seller.getVerification_status()) || !"ACTIVE".equals(seller.getStatus())) throw new ApiException("Restaurant is not available");
        MenuItem item = menuItemRepository.findById(request.menuItemId()).orElseThrow(() -> new ApiException("Menu item not found"));
        if (!item.getMenu().getSeller().getId().equals(sellerId)) throw new ApiException("Item belongs to a different restaurant");
        if (!Boolean.TRUE.equals(item.getAvailable())) throw new ApiException("Menu item is unavailable");

        Cart cart = cartRepository.findByUser_IdAndSeller_IdAndStatus(userId, sellerId, "PENDING").orElseGet(() -> {
            Cart c = new Cart(); c.setUser(user); c.setSeller(seller); c.setStatus("PENDING"); c.setTotalAmount(BigDecimal.ZERO); c.setExpiresAt(LocalDateTime.now().plusHours(24)); return cartRepository.save(c);
        });
        if (cart.getExpiresAt().isBefore(LocalDateTime.now())) throw new ApiException("Cart expired; create a new cart");
        CartItem ci = cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), item.getId()).orElseGet(() -> {
            CartItem c = new CartItem(); c.setCart(cart); c.setMenuItem(item); c.setQuantity(0); c.setUnitPrice(item.getPrice()); return c;
        });
        ci.setQuantity(ci.getQuantity() + request.quantity()); ci.setUnitPrice(item.getPrice()); cartItemRepository.save(ci);
        recalculate(cart); return cartRepository.save(cart);
    }

    public List<Cart> userCarts(Integer userId) {
        if (!userRepository.existsById(userId)) throw new ApiException("User not found");
        return cartRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public Cart updateQuantity(Integer userId, Integer cartId, Integer itemId, Integer quantity) {
        if (quantity == null || quantity < 1) throw new ApiException("Quantity must be at least 1");
        Cart cart = ownedCart(userId, cartId); ensureEditable(cart);
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Cart item not found"));
        if (!item.getCart().getId().equals(cartId)) throw new ApiException("Cart item does not belong to this cart");
        item.setQuantity(quantity); cartItemRepository.save(item); recalculate(cart); return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Integer userId, Integer cartId, Integer itemId) {
        Cart cart = ownedCart(userId, cartId); ensureEditable(cart);
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Cart item not found"));
        if (!item.getCart().getId().equals(cartId)) throw new ApiException("Cart item does not belong to this cart");
        cart.getItems().removeIf(i -> i.getId().equals(itemId)); cartItemRepository.delete(item); recalculate(cart); return cartRepository.save(cart);
    }

    @Transactional
    public void clear(Integer userId, Integer cartId) {
        Cart cart = ownedCart(userId, cartId); ensureEditable(cart); cart.getItems().clear(); cart.setTotalAmount(BigDecimal.ZERO); cartRepository.save(cart);
    }

    private Cart ownedCart(Integer userId, Integer cartId) {
        Cart c = cartRepository.findById(cartId).orElseThrow(() -> new ApiException("Cart not found"));
        if (!c.getUser().getId().equals(userId)) throw new ApiException("Cart does not belong to this user"); return c;
    }
    private void ensureEditable(Cart c) { if (!"PENDING".equals(c.getStatus())) throw new ApiException("Cart is not editable"); if (c.getExpiresAt().isBefore(LocalDateTime.now())) throw new ApiException("Cart expired"); }
    private void recalculate(Cart c) { BigDecimal total = c.getItems().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); c.setTotalAmount(total); }
}
