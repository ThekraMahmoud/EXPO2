package com.example.expo.Service;
import com.example.expo.DTO.CartResponse;

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

        //لاضافة العنصر الى السلة
        //نتأكد من ان المستخدم موجود
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        //نتأكد من وجود البائع
        Sellerers seller = sellersRepository.findById(sellerId).orElseThrow(() -> new ApiException("Restaurant not found"));


        if (!"VERIFIED".equals(seller.getVerification_status()) || !"ACTIVE".equals(seller.getStatus())) throw new ApiException("Restaurant is not available");

        //نتأد
        MenuItem item = menuItemRepository.findById(request.menuItemId()).orElseThrow(() -> new ApiException("Menu item not found"));
        if (!item.getMenu().getSeller().getId().equals(sellerId)) throw new ApiException("Item belongs to a different restaurant");
        if (!Boolean.TRUE.equals(item.getAvailable())) throw new ApiException("Menu item is unavailable");

        Cart cart = cartRepository.findPendingForUpdate(userId, sellerId).orElseGet(() -> {
            Cart c = new Cart(); c.setUser(user); c.setSeller(seller); c.setStatus("PENDING"); c.setTotalAmount(BigDecimal.ZERO); c.setExpiresAt(LocalDateTime.now().plusHours(24)); return cartRepository.save(c);
        });
        if (cart.getExpiresAt().isBefore(LocalDateTime.now())) throw new ApiException("Cart expired; create a new cart");
        CartItem ci = cartItemRepository.findByCart_IdAndMenuItem_Id(cart.getId(), item.getId()).orElseGet(() -> {
            CartItem c = new CartItem(); c.setCart(cart); c.setMenuItem(item); c.setQuantity(0); c.setUnitPrice(item.getPrice()); return c;
        });
        // العلاقة ثنائية الاتجاه: أضف العنصر للمجموعة قبل إعادة حساب الإجمالي.
        if (!cart.getItems().contains(ci)) cart.getItems().add(ci);
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
        Cart cart = ownedCartForUpdate(userId, cartId); ensureEditable(cart);
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Cart item not found"));
        if (!item.getCart().getId().equals(cartId)) throw new ApiException("Cart item does not belong to this cart");
        // تعديل السلة فعل صريح من المستخدم؛ نعرض السعر الحالي قبل تأكيد الشراء التالي.
        MenuItem product = item.getMenuItem();
        if (product == null || !Boolean.TRUE.equals(product.getAvailable()) || product.getPrice() == null || product.getPrice().signum() <= 0)
            throw new ApiException("Menu item is unavailable");
        item.setUnitPrice(product.getPrice());
        item.setQuantity(quantity); cartItemRepository.save(item); recalculate(cart); return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(Integer userId, Integer cartId, Integer itemId) {
        Cart cart = ownedCartForUpdate(userId, cartId); ensureEditable(cart);
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new ApiException("Cart item not found"));
        if (!item.getCart().getId().equals(cartId)) throw new ApiException("Cart item does not belong to this cart");
        cart.getItems().removeIf(i -> i.getId().equals(itemId)); cartItemRepository.delete(item); recalculate(cart); return cartRepository.save(cart);
    }

    @Transactional
    public void clear(Integer userId, Integer cartId) {
        Cart cart = ownedCartForUpdate(userId, cartId); ensureEditable(cart); cart.getItems().clear(); cart.setTotalAmount(BigDecimal.ZERO); cartRepository.save(cart);
    }

    private Cart ownedCart(Integer userId, Integer cartId) {
        Cart c = cartRepository.findById(cartId).orElseThrow(() -> new ApiException("Cart not found"));
        if (!c.getUser().getId().equals(userId)) throw new ApiException("Cart does not belong to this user"); return c;
    }
    private void ensureEditable(Cart c) { if (!"PENDING".equals(c.getStatus())) throw new ApiException("Cart is not editable"); if (c.getExpiresAt().isBefore(LocalDateTime.now())) throw new ApiException("Cart expired"); }
    private void recalculate(Cart c) { BigDecimal total = c.getItems().stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); c.setTotalAmount(total); }

    // عرض سلة واحدة دون تغيير استجابات مسارات السلة القديمة.
    @Transactional(readOnly = true)
    public CartResponse getCartById(Integer userId, Integer cartId) {
        Cart cart = ownedCart(userId, cartId);
        return CartResponse.from(cart, "Cart retrieved successfully");
    }


    private Cart ownedCartForUpdate(Integer userId, Integer cartId) {
        Cart cart = cartRepository.findByIdForUpdate(cartId).orElseThrow(() -> new ApiException("Cart not found"));
        if (!cart.getUser().getId().equals(userId)) throw new ApiException("Cart does not belong to this user");
        return cart;
    }


    @Transactional
    public CartResponse addItemOutput(Integer userId, Integer sellerId, CartItemRequest request) { return CartResponse.from(addItem(userId, sellerId, request), "Cart items added successfully"); }


    @Transactional(readOnly = true)
    public List<CartResponse> userCartOutputs(Integer userId) { return userCarts(userId).stream().map(c -> CartResponse.from(c, "Cart retrieved successfully")).toList(); }


    @Transactional
    public CartResponse updateQuantityOutput(Integer userId, Integer cartId, Integer itemId, Integer quantity) { return CartResponse.from(updateQuantity(userId, cartId, itemId, quantity), "Cart item updated successfully"); }


    @Transactional
    public CartResponse removeItemOutput(Integer userId, Integer cartId, Integer itemId) { return CartResponse.from(removeItem(userId, cartId, itemId), "Cart item removed successfully"); }


    @Transactional
    public CartResponse clearOutput(Integer userId, Integer cartId) {
        clear(userId, cartId);
        return CartResponse.from(ownedCart(userId, cartId), "Cart cleared successfully");
    }

}