package com.example.expo.DTO;
import com.example.expo.Model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
// استجابة قراءة محددة؛ لا نعيد Order المتداخل وحساب مستخدمه.
public record PaymentRecordResponse(Integer paymentId, Integer orderId, BigDecimal amount, String paymentMethod,
                                    String status, String transactionReference, LocalDateTime createdAt) {
    public static PaymentRecordResponse from(Payment payment) {
        if (payment == null) return null;
        return new PaymentRecordResponse(payment.getId(), payment.getOrder() == null ? null : payment.getOrder().getId(),
                payment.getAmount(), payment.getPaymentMethod(), payment.getStatus(), payment.getTransactionReference(), payment.getCreatedAt());
    }
}
