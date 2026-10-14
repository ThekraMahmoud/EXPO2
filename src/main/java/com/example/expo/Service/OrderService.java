        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
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


    @Transactional
    public Order checkout(Integer userId, Integer cartId, CheckoutRequest request) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ApiException("Cart not found"));

        if (!cart.getUser().getId().equals(userId)) {
            throw new ApiException("Cart does not belong to this user");
        }

        if (!"PENDING".equals(cart.getStatus())) {
            throw new ApiException("Cart has already been checked out");
        }

        if (cart.getExpiresAt() != null
                && cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Cart expired");
        }

        if (cart.getItems().isEmpty()) {
            throw new ApiException("Cannot checkout an empty cart");
        }

        if (request.paymentMethod() == null
                || request.paymentMethod().isBlank()) {
            throw new ApiException("Payment method is required");
        }

        String method = request.paymentMethod().trim().toUpperCase();

        if (!List.of("MOYASAR", "WALLET", "APPLE_PAY", "VISA", "ON_SITE")
                .contains(method)) {
            throw new ApiException("Unsupported payment method");
        }

        Order order = new Order();

        order.setUser(cart.getUser());
        order.setSeller(cart.getSeller());
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus("NEW");
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod(method);

        for (CartItem cartItem : cart.getItems()) {

            MenuItem menuItem = cartItem.getMenuItem();

            if (menuItem == null || !menuItem.getAvailable()) {
                throw new ApiException("One of the cart items is unavailable");
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemName(menuItem.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());

            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        if ("WALLET".equals(method)) {

            UserWallets userWallet =
                    userWalletsRepository.findUserWalletsByUserId(userId);

            if (userWallet == null
                    || !"ACTIVE".equals(userWallet.getStatus())) {
                throw new ApiException("Active user wallet not found");
            }

            if (userWallet.getBalance().compareTo(savedOrder.getTotalAmount()) < 0) {
                throw new ApiException("Insufficient wallet balance");
            }

            Integer sellerId = savedOrder.getSeller().getId();

            SellerersWallets sellerWallet = sellersWalletsRepository.findAll()
                    .stream()
                    .filter(wallet -> wallet.getSeller().getId().equals(sellerId))
                    .findFirst()
                    .orElseThrow(() ->
                            new ApiException("Restaurant wallet not found; contact support"));

            if (!"ACTIVE".equals(sellerWallet.getStatus())) {
                throw new ApiException("Restaurant wallet is suspended");
            }

            userWallet.setBalance(
                    userWallet.getBalance().subtract(savedOrder.getTotalAmount())
            );

            sellerWallet.setBalance(
                    sellerWallet.getBalance().add(savedOrder.getTotalAmount())
            );

            userWalletsRepository.save(userWallet);
            sellersWalletsRepository.save(sellerWallet);

            UserWalletTransaction debit = new UserWalletTransaction();

            debit.setUserWallets(userWallet);
            debit.setTransactionType("PAYMENT");
            debit.setAmount(savedOrder.getTotalAmount());
            debit.setStatus("SUCCESS");
            debit.setReference(
                    "ORDER-" + savedOrder.getId() + "-USER-" + UUID.randomUUID()
            );

            userWalletTransactionRepository.save(debit);

            SellerWallerTransaction credit = new SellerWallerTransaction();

            credit.setSellerWallet(sellerWallet);
            credit.setAmount(savedOrder.getTotalAmount());
            credit.setType("topup");
            credit.setCreated_at(LocalDateTime.now());

            sellerWallerTransactionRepository.save(credit);

            savedOrder.setPaymentStatus("PAID");

            Order paidOrder = orderRepository.save(savedOrder);

            createInvoice(paidOrder);

            savedOrder = paidOrder;
        }

        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        return savedOrder;
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

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        if (!order.getSeller().getId().equals(sellerId)) {
            throw new ApiException("Order does not belong to this restaurant");
        }

        String nextStatus = status == null ? "" : status.trim().toUpperCase();

        if (!List.of("ACCEPTED", "PREPARING", "DELAYED",
                "READY", "COMPLETED", "CANCELLED").contains(nextStatus)) {
            throw new ApiException("Invalid order status");
        }

        if ("COMPLETED".equals(order.getStatus())
                || "CANCELLED".equals(order.getStatus())) {
            throw new ApiException("Order is already closed");
        }

        if ("CANCELLED".equals(nextStatus)
                && "PAID".equals(order.getPaymentStatus())) {
            throw new ApiException(
                    "Paid orders require a refund workflow before cancellation"
            );
        }

        order.setStatus(nextStatus);

        return orderRepository.save(order);
    }


    @Transactional
    public Invoices confirmMoyasarPayment(
            Integer userId,
            Integer orderId,
            String paymentId) {

        Order order = getCustomerOrder(userId, orderId);

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
    public Invoices confirmOnSitePayment(Integer sellerId, Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found"));

        if (!order.getSeller().getId().equals(sellerId)) {
            throw new ApiException("Order does not belong to this restaurant");
        }

        if (!"ON_SITE".equals(order.getPaymentMethod())) {
            throw new ApiException(
                    "This order is not configured for on-site payment"
            );
        }

        if ("PAID".equals(order.getPaymentStatus())) {
            return invoicesRepository.findByOrder_Id(orderId)
                    .orElseThrow(() ->
                            new ApiException("Paid order invoice is missing"));
        }

        order.setPaymentStatus("PAID");

        Order paidOrder = orderRepository.save(order);

        return createInvoice(paidOrder);
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

        invoice.setDescription("Food order #" + order.getId());
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

}