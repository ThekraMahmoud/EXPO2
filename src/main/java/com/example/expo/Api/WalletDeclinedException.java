package com.example.expo.Api;
import com.example.expo.DTO.PaymentResponse;
import com.example.expo.Model.Cart;
import java.math.BigDecimal;
import java.util.List;

// يحمل لقطة آمنة للمحاولة المرفوضة؛ لا يحمل كيان JPA بعد انتهاء المعاملة.
public class WalletDeclinedException extends ApiException {
    public final BigDecimal amount;
    public final String sellerName;
    public final List<PaymentResponse.ItemOutput> items;
    public WalletDeclinedException(String message, Cart cart) {
        super(message); amount = cart.getTotalAmount(); sellerName = cart.getSeller().getBusiness_name();
        items = cart.getItems().stream().map(i -> new PaymentResponse.ItemOutput(
                i.getMenuItem().getName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal())).toList();
    }
}
