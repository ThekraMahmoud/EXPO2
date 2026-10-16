        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Api.WalletDeclinedException;
import com.example.expo.DTO.PaymentConfirmationResponse;
import com.example.expo.Client.MoyasarClient;
import com.example.expo.DTO.CheckoutRequest;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SellersRepository sellersRepository;
    private final InvoicesRepository invoicesRepository;
    private final ObjectProvider<MoyasarClient> moyasarClientProvider;
    private final ObjectMapper objectMapper;
    private final UserWalletsRepository userWalletsRepository;
    private final SellersWalletsRepository sellersWalletsRepository;
    private final UserWalletTransactionRepository userWalletTransactionRepository;
    private final SellerWallerTransactionRepository sellerWallerTransactionRepository;


    // المصدر الوحيد لإنشاء الطلب والخصم، لكل من checkout ومسار دفع السلة الجديد.
    @Transactional
    public Order checkout(Integer userId, Integer cartId, CheckoutRequest request) {
        Cart cart = cartRepository.findByIdForUpdate(cartId).orElseThrow(() -> new ApiException("Cart not found"));
        if (!cart.getUser().getId().equals(userId)) throw new ApiException("Cart does not belong to this user");
        if (!"PENDING".equals(cart.getStatus())) throw new ApiException("Cart has already been checked out");
        if (cart.getExpiresAt() != null && cart.getExpiresAt().isBefore(LocalDateTime.now())) throw new ApiException("Cart expired");
        if (cart.getItems().isEmpty()) throw new ApiException("Cannot checkout an empty cart");
        if (!"ACTIVE".equals(cart.getSeller().getStatus()) || !"VERIFIED".equals(cart.getSeller().getVerification_status()))
            throw new ApiException("Seller is not available");
        String method = request == null || request.paymentMethod() == null ? "" : request.paymentMethod().trim().toUpperCase(java.util.Locale.ROOT);
        if (!List.of("MOYASAR", "WALLET", "APPLE_PAY", "VISA", "ON_SITE").contains(method)) throw new ApiException("Unsupported payment method");
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            MenuItem product = item.getMenuItem();
            if (product == null || !Boolean.TRUE.equals(product.getAvailable())) throw new ApiException("One of the cart items is unavailable");
            if (!product.getMenu().getSeller().getId().equals(cart.getSeller().getId())) throw new ApiException("Item belongs to a different seller");
            if (item.getQuantity() == null || item.getQuantity() < 1 || item.getUnitPrice() == null || item.getUnitPrice().signum() <= 0)
                throw new ApiException("Invalid cart item quantity or price");
            if (product.getPrice() == null || product.getPrice().compareTo(item.getUnitPrice()) != 0)
                throw new ApiException("Cart item price has changed; update the cart");
            total = total.add(item.getSubtotal());
        }
        if (total.signum() <= 0 || total.compareTo(new BigDecimal("99999999.99")) > 0) throw new ApiException("Invalid cart total");
        cart.setTotalAmount(total);
        UserWallets userWallet = null; SellerersWallets sellerWallet = null;
        if ("WALLET".equals(method)) {
            // ترتيب الأقفال ثابت: السلة ثم محفظة المستخدم ثم محفظة المؤسسة.
            userWallet = userWalletsRepository.findByUserIdForUpdate(userId).orElse(null);
            if (userWallet == null || !"ACTIVE".equals(userWallet.getStatus())) throw new WalletDeclinedException("Active user wallet not found", cart);
            if (userWallet.getBalance() == null || userWallet.getBalance().compareTo(total) < 0) throw new WalletDeclinedException("Insufficient wallet balance", cart);
            sellerWallet = sellersWalletsRepository.findBySellerIdForUpdate(cart.getSeller().getId()).orElse(null);
            if (sellerWallet == null || !"ACTIVE".equals(sellerWallet.getStatus()) || sellerWallet.getBalance() == null)
                throw new WalletDeclinedException("Active seller wallet not found", cart);
            if (!"SAR".equals(sellerWallet.getCurrency())) throw new ApiException("Seller wallet currency must be SAR");
        }
        Order order = new Order(); order.setUser(cart.getUser()); order.setSeller(cart.getSeller());
        order.setTotalAmount(total); order.setStatus("NEW"); order.setPaymentStatus("PENDING"); order.setPaymentMethod(method);
        for (CartItem item : cart.getItems()) {
            OrderItem snapshot = new OrderItem(); snapshot.setOrder(order); snapshot.setMenuItem(item.getMenuItem());
            snapshot.setItemName(item.getMenuItem().getName()); snapshot.setQuantity(item.getQuantity()); snapshot.setUnitPrice(item.getUnitPrice());
            order.getItems().add(snapshot);
        }
        Order saved = orderRepository.save(order);
        if ("WALLET".equals(method)) {
            userWallet.setBalance(userWallet.getBalance().subtract(total));
            sellerWallet.setBalance(sellerWallet.getBalance().add(total)); sellerWallet.setUpdated_at(LocalDateTime.now());
            userWalletsRepository.save(userWallet); sellersWalletsRepository.save(sellerWallet);
            UserWalletTransaction debit = new UserWalletTransaction(); debit.setUserWallets(userWallet); debit.setTransactionType("PAYMENT");
            debit.setAmount(total); debit.setStatus("SUCCESS"); debit.setReference("ORDER-" + saved.getId() + "-USER-" + UUID.randomUUID());
            userWalletTransactionRepository.save(debit);
            SellerWallerTransaction credit = new SellerWallerTransaction(); credit.setSellerWallet(sellerWallet); credit.setAmount(total);
            credit.setType("TOP_UP"); credit.setStatus("SUCCESS"); credit.setCreated_at(LocalDateTime.now());
            credit.setReference("ORDER-" + saved.getId() + "-SELLER-" + UUID.randomUUID()); sellerWallerTransactionRepository.save(credit);
            saved.setPaymentStatus("PAID"); orderRepository.save(saved); createInvoice(saved);
            Payment payment = new Payment(); payment.setOrder(saved); payment.setAmount(total); payment.setPaymentMethod("WALLET");
            payment.setStatus("SUCCESS"); payment.setTransactionReference("ORDER-" + saved.getId() + "-WALLET"); paymentRepository.save(payment);
        }
        // نغلق السلة بدل إعادة فتحها؛ الطلب يحتفظ بلقطة المنتجات ولا يمكن خصم السلة نفسها مرتين.
        cart.setStatus("CHECKED_OUT"); cartRepository.save(cart);
        return saved;
    }

    public List<Order> customerOrders(Integer userId) {

        if (!userRepository.existsById(userId)) {
            throw new ApiException("User not found");
        }

        return orderRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
    }


    public List<Order> sellerOrders(Integer sellerId) {

        if (!sellersRepository.existsById(sellerId)) {
            throw new ApiException("Restaurant not found");
        }

        return orderRepository.findAllBySeller_IdOrderByCreatedAtDesc(sellerId);
    }


    public Order getCustomerOrder(Integer userId, Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ApiException("Order does not belong to this user");
        }

        return order;
    }


    @Transactional
    public Order updateSellerStatus(Integer sellerId, Integer orderId, String status) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new ApiException("Order not found"));
        if (!order.getSeller().getId().equals(sellerId)) throw new ApiException("Order does not belong to this restaurant");
        String next = status == null ? "" : status.trim().toUpperCase(java.util.Locale.ROOT);
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) throw new ApiException("Order is already closed");
        if ("CANCELLED".equals(next) && "PAID".equals(order.getPaymentStatus())) throw new ApiException("Paid orders require a refund workflow before cancellation");
        boolean valid = switch (order.getStatus()) {
            case "NEW" -> List.of("ACCEPTED", "CANCELLED").contains(next);
            case "ACCEPTED" -> List.of("PREPARING", "DELAYED", "CANCELLED").contains(next);
            case "PREPARING" -> List.of("READY", "DELAYED", "CANCELLED").contains(next);
            case "DELAYED" -> List.of("PREPARING", "READY", "CANCELLED").contains(next);
            case "READY" -> "COMPLETED".equals(next);
            default -> false;
        };
        if (!valid) throw new ApiException("Invalid order status transition: " + order.getStatus() + " -> " + next);
        if ("COMPLETED".equals(next) && !"PAID".equals(order.getPaymentStatus())) throw new ApiException("Order must be paid before completion");
        order.setStatus(next); return orderRepository.save(order);
    }

    @Transactional
    public Invoices confirmMoyasarPayment(
            Integer userId,
            Integer orderId,
            String paymentId) {

        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new ApiException("Order not found"));
        if (!order.getUser().getId().equals(userId)) throw new ApiException("Order does not belong to this user");

        if (!List.of("MOYASAR", "VISA", "APPLE_PAY")
                .contains(order.getPaymentMethod())) {
            throw new ApiException(
                    "This order is not configured for online payment"
            );
        }

        if ("PAID".equals(order.getPaymentStatus())) {
            return invoicesRepository.findByOrder_Id(orderId)
                    .orElseThrow(() ->
                            new ApiException("Paid order invoice is missing"));
        }

        if (paymentId == null || paymentId.isBlank()) {
            throw new ApiException("Payment ID is required");
        }

        MoyasarClient moyasarClient =
                moyasarClientProvider.getIfAvailable();

        if (moyasarClient == null) {
            throw new ApiException("Moyasar is currently disabled");
        }

        try {

            JsonNode payment = objectMapper.readTree(
                    moyasarClient.verifyPayment(paymentId)
            );

            String paymentStatus = payment.path("status").asString("");
            long amountHalalas = payment.path("amount").asLong(-1);
            String currency = payment.path("currency").asString("");

            long expectedHalalas = order.getTotalAmount()
                    .multiply(new BigDecimal("100"))
                    .longValueExact();

            if (!"paid".equalsIgnoreCase(paymentStatus)) {
                throw new ApiException("Payment is not paid at Moyasar");
            }

            if (amountHalalas != expectedHalalas
                    || !"SAR".equalsIgnoreCase(currency)) {
                throw new ApiException(
                        "Payment amount or currency does not match order"
                );
            }

            order.setPaymentStatus("PAID");

            Order paidOrder = orderRepository.save(order);

            return createInvoice(paidOrder);

        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(
                    "Could not verify payment with Moyasar. Check payment ID and server configuration"
            );
        }
    }


    @Transactional
    public PaymentConfirmationResponse confirmOnSitePayment(Integer sellerId, Integer orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new ApiException("Order not found"));
        if (!order.getSeller().getId().equals(sellerId)) throw new ApiException("Order does not belong to this restaurant");
        if (!"ON_SITE".equals(order.getPaymentMethod())) throw new ApiException("This order is not configured for on-site payment");
        if ("CANCELLED".equals(order.getStatus())) throw new ApiException("Cancelled order cannot be paid");
        if (!"PAID".equals(order.getPaymentStatus())) { order.setPaymentStatus("PAID"); orderRepository.save(order); }
        Invoices invoice = createInvoice(order);
        return new PaymentConfirmationResponse("Payment confirmed successfully", order.getId(), order.getPaymentStatus(), order.getStatus(), invoice.getId());
    }

    private Invoices createInvoice(Order order) {

        Invoices existingInvoice = invoicesRepository.findByOrder_Id(order.getId())
                .orElse(null);

        if (existingInvoice != null) {
            return existingInvoice;
        }

        Invoices invoice = new Invoices();

        invoice.setInvoice_number(
                "EXP-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 12)
                        .toUpperCase()
        );

        invoice.setDescription("Order #" + order.getId());
        invoice.setSubtotal(order.getTotalAmount());
        invoice.setTax_amount(BigDecimal.ZERO);
        invoice.setTotal_amount(order.getTotalAmount());
        invoice.setStatus("PAID");
        invoice.setQr_token(UUID.randomUUID().toString());
        invoice.setPaid_at(LocalDateTime.now());
        invoice.setSeller(order.getSeller());
        invoice.setUser(order.getUser());
        invoice.setOrder(order);

        return invoicesRepository.save(invoice);
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

}