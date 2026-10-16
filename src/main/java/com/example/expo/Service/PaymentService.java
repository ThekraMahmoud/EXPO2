package com.example.expo.Service;
import com.example.expo.Api.ApiException;
import com.example.expo.Api.WalletDeclinedException;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    public List<Payment> getAllPayments() { return paymentRepository.findAll(); }
    public Payment getPaymentById(Integer id) { return paymentRepository.findById(id).orElseThrow(() -> new ApiException("Payment not found")); }
    public List<Payment> getPaymentsByOrder(Integer id) {
        if (!orderRepository.existsById(id)) throw new ApiException("Order not found");
        return paymentRepository.findAllByOrder_Id(id);
    }
    public List<Payment> getPaymentsByStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!List.of("SUCCESS", "FAILED", "PENDING").contains(normalized)) throw new ApiException("Invalid payment status");
        return paymentRepository.findAllByStatus(normalized);
    }
    // لا تفتح هذه الواجهة معاملة حول checkout؛ يجب انتهاء rollback قبل حفظ محاولة الرفض.
    public PaymentResponse payCart(Integer userId, Integer cartId) {
        try {
            Order order = orderService.checkout(userId, cartId, new CheckoutRequest("WALLET"));
            Payment payment = paymentRepository.findAllByOrder_Id(order.getId()).stream()
                    .filter(p -> "SUCCESS".equals(p.getStatus())).findFirst()
                    .orElseThrow(() -> new ApiException("Payment record not found"));
            var items = order.getItems().stream().map(i -> new PaymentResponse.ItemOutput(i.getItemName(), i.getQuantity(),
                    i.getUnitPrice(), i.getUnitPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity())))).toList();
            return new PaymentResponse(payment.getId(), order.getSeller().getBusiness_name(), items, payment.getAmount(), payment.getStatus(), order.getId(), "Payment completed successfully");
        } catch (org.springframework.dao.ConcurrencyFailureException conflict) {
            throw new ApiException("Wallet or cart changed during payment; refresh and try again");
        } catch (WalletDeclinedException declined) {
            Payment failed = new Payment(); failed.setAmount(declined.amount); failed.setPaymentMethod("WALLET");
            failed.setStatus("FAILED"); failed.setTransactionReference("DECLINED-" + UUID.randomUUID());
            Payment saved = paymentRepository.save(failed);
            return new PaymentResponse(saved.getId(), declined.sellerName, declined.items, saved.getAmount(), "FAILED", null, declined.getMessage());
        }
    }
}
