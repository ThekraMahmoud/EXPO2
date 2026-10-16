package com.example.expo.DTO;
import com.example.expo.Model.Cart;
import java.math.BigDecimal;
import java.util.List;

// المعرفات لازمة لتعديل العناصر وحذفها؛ لا نعيد كيانات المستخدم أو المؤسسة.
public record CartResponse(String message, Integer cartId, String sellerName, List<Item> items,
                           BigDecimal totalAmount, String status) {
    public record Item(Integer cartItemId, Integer menuItemId, String name,
                       Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
    public CartResponse(String message, Integer cartId, String sellerName, List<Item> items) {
        this(message, cartId, sellerName, items, null, null);
    }
    public static CartResponse from(Cart cart, String message) {
        return new CartResponse(message, cart.getId(), cart.getSeller().getBusiness_name(),
                cart.getItems().stream().map(i -> new Item(i.getId(), i.getMenuItem().getId(),
                        i.getMenuItem().getName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal())).toList(),
                cart.getTotalAmount(), cart.getStatus());
    }
}
