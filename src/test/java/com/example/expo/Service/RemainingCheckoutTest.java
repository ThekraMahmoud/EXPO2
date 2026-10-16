package com.example.expo.Service;
import com.example.expo.Api.*;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import com.example.expo.Client.MoyasarClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemainingCheckoutTest {
    PaymentRepository payments = mock(PaymentRepository.class);
    CartRepository carts = mock(CartRepository.class);
    OrderRepository orders = mock(OrderRepository.class);
    InvoicesRepository invoices = mock(InvoicesRepository.class);
    UserWalletsRepository usersWallet = mock(UserWalletsRepository.class);
    SellersWalletsRepository sellersWallet = mock(SellersWalletsRepository.class);
    UserWalletTransactionRepository userTx = mock(UserWalletTransactionRepository.class);
    SellerWallerTransactionRepository sellerTx = mock(SellerWallerTransactionRepository.class);
    OrderService service;
    Cart cart; UserWallets uw; SellerersWallets sw;
    @BeforeEach void setup() {
        service = new OrderService(payments, carts, orders, mock(UserRepository.class), mock(SellersRepository.class),
                invoices, mock(ObjectProvider.class), JsonMapper.builder().build(), usersWallet, sellersWallet, userTx, sellerTx);
        User user = new User(); user.setId(1);
        Sellerers seller = new Sellerers(); seller.setId(7); seller.setBusiness_name("Store"); seller.setStatus("ACTIVE"); seller.setVerification_status("VERIFIED");
        Menu menu = new Menu(); menu.setSeller(seller);
        MenuItem product = new MenuItem(); product.setId(3); product.setName("Book"); product.setMenu(menu); product.setAvailable(true); product.setPrice(BigDecimal.TEN);
        cart = new Cart(); cart.setId(4); cart.setUser(user); cart.setSeller(seller); cart.setStatus("PENDING");
        cart.setTotalAmount(new BigDecimal("999")); cart.setExpiresAt(LocalDateTime.now().plusHours(1));
        CartItem line = new CartItem(); line.setCart(cart); line.setMenuItem(product); line.setQuantity(2); line.setUnitPrice(BigDecimal.TEN); cart.getItems().add(line);
        uw = new UserWallets(); uw.setBalance(new BigDecimal("100")); uw.setStatus("ACTIVE"); uw.setUser(user);
        sw = new SellerersWallets(); sw.setBalance(new BigDecimal("50")); sw.setSeller(seller);
        when(carts.findByIdForUpdate(4)).thenReturn(Optional.of(cart));
        when(usersWallet.findByUserIdForUpdate(1)).thenReturn(Optional.of(uw));
        when(sellersWallet.findBySellerIdForUpdate(7)).thenReturn(Optional.of(sw));
        when(orders.save(any())).thenAnswer(c -> {Order order = c.getArgument(0); order.setId(10); return order;});
        when(invoices.save(any())).thenAnswer(c -> {Invoices invoice = c.getArgument(0); invoice.setId(44); return invoice;});
    }
    @Test void walletCheckoutRecalculatesTotalRecordsCompatibleLedgerAndKeepsOrderNew() {
        Order order = service.checkout(1, 4, new CheckoutRequest(" wallet "));
        assertEquals(new BigDecimal("20"), order.getTotalAmount()); assertEquals("NEW", order.getStatus()); assertEquals("PAID", order.getPaymentStatus());
        assertEquals(new BigDecimal("80"), uw.getBalance()); assertEquals(new BigDecimal("70"), sw.getBalance()); assertNotNull(sw.getUpdated_at());
        assertEquals("CHECKED_OUT", cart.getStatus()); assertEquals(1, order.getItems().size());
        verify(sellerTx).save(argThat(t -> "TOP_UP".equals(t.getType()) && "SUCCESS".equals(t.getStatus()) && t.getReference() != null));
        verify(userTx).save(argThat(t -> "PAYMENT".equals(t.getTransactionType()) && t.getReference() != null));
        verify(payments).save(argThat(p -> "SUCCESS".equals(p.getStatus()) && p.getOrder() == order));
        var locks = inOrder(carts, usersWallet, sellersWallet);
        locks.verify(carts).findByIdForUpdate(4); locks.verify(usersWallet).findByUserIdForUpdate(1); locks.verify(sellersWallet).findBySellerIdForUpdate(7);
        assertThrows(ApiException.class, () -> service.checkout(1, 4, new CheckoutRequest("WALLET")));
        verify(usersWallet, times(1)).save(any()); verify(payments, times(1)).save(any());
    }
    @Test void insufficientBalanceDoesNotCreateOrderInvoiceOrMoveMoney() {
        uw.setBalance(BigDecimal.ONE);
        assertThrows(WalletDeclinedException.class, () -> service.checkout(1, 4, new CheckoutRequest("WALLET")));
        verify(orders, never()).save(any()); verify(invoices, never()).save(any()); verify(usersWallet, never()).save(any());
        assertEquals("PENDING", cart.getStatus()); assertEquals(new BigDecimal("50"), sw.getBalance());
    }
    @Test void changedPriceAndWrongOwnerAreRejectedBeforePayment() {
        cart.getItems().get(0).getMenuItem().setPrice(new BigDecimal("12"));
        assertThrows(ApiException.class, () -> service.checkout(1, 4, new CheckoutRequest("WALLET")));
        assertThrows(ApiException.class, () -> service.checkout(99, 4, new CheckoutRequest("WALLET")));
        verifyNoInteractions(usersWallet, sellersWallet, payments);
    }
    @Test void statusTransitionsAreOrderedAndCompletionRequiresPayment() {
        Order order = service.checkout(1, 4, new CheckoutRequest("ON_SITE"));
        when(orders.findByIdForUpdate(10)).thenReturn(Optional.of(order));
        assertThrows(ApiException.class, () -> service.updateSellerStatus(7, 10, "READY"));
        service.updateSellerStatus(7, 10, " accepted "); service.updateSellerStatus(7, 10, "PREPARING"); service.updateSellerStatus(7, 10, "READY");
        assertThrows(ApiException.class, () -> service.updateSellerStatus(7, 10, "COMPLETED"));
        var response = service.confirmOnSitePayment(7, 10);
        assertEquals("READY", order.getStatus()); assertEquals("PAID", response.paymentStatus()); assertEquals(44, response.invoiceId());
        service.updateSellerStatus(7, 10, "COMPLETED"); assertEquals("COMPLETED", order.getStatus());
    }
    @Test void paidOrderCannotBeCancelledAndOtherSellerCannotChangeStatus() {
        Order order = service.checkout(1, 4, new CheckoutRequest("WALLET"));
        when(orders.findByIdForUpdate(10)).thenReturn(Optional.of(order));
        assertThrows(ApiException.class, () -> service.updateSellerStatus(7, 10, "CANCELLED"));
        assertThrows(ApiException.class, () -> service.updateSellerStatus(99, 10, "ACCEPTED"));
    }
    @Test void cartPaymentEndpointDelegatesToSameCheckoutAndRecordsDeclinesAfterItFails() {
        OrderService core = mock(OrderService.class); PaymentService facade = new PaymentService(payments, orders, core);
        when(core.checkout(eq(1), eq(4), any())).thenThrow(new WalletDeclinedException("Insufficient wallet balance", cart));
        when(payments.save(any())).thenAnswer(c -> {Payment payment = c.getArgument(0); payment.setId(8); return payment;});
        PaymentResponse response = facade.payCart(1, 4);
        assertEquals("FAILED", response.status()); assertEquals(8, response.paymentId());
        verify(core).checkout(1, 4, new CheckoutRequest("WALLET"));
        verify(payments).save(argThat(p -> "FAILED".equals(p.getStatus()) && p.getOrder() == null));
        verifyNoInteractions(userTx, sellerTx);
    }
    @Test void cartOutputIncludesNewItemInTotalAndIncludesIdsForEditing() {
        UserRepository users = mock(UserRepository.class); SellersRepository sellers = mock(SellersRepository.class);
        MenuItemRepository products = mock(MenuItemRepository.class); CartItemRepository lines = mock(CartItemRepository.class);
        MenuItem product = cart.getItems().get(0).getMenuItem(); cart.getItems().clear();
        when(users.findById(1)).thenReturn(Optional.of(cart.getUser())); when(sellers.findById(7)).thenReturn(Optional.of(cart.getSeller()));
        when(products.findById(3)).thenReturn(Optional.of(product)); when(carts.findPendingForUpdate(1, 7)).thenReturn(Optional.of(cart));
        when(lines.save(any())).thenAnswer(c -> {CartItem item = c.getArgument(0); item.setId(8); return item;}); when(carts.save(any())).thenAnswer(c -> c.getArgument(0));
        CartService cartService = new CartService(carts, lines, users, sellers, products);
        var result = cartService.addItemOutput(1, 7, new CartItemRequest(3, 2));
        assertEquals(new BigDecimal("20"), result.totalAmount()); assertEquals(8, result.items().get(0).cartItemId());
        assertEquals(3, result.items().get(0).menuItemId()); assertEquals("PENDING", result.status());
    }
    @Test void editingQuantityRefreshesUnitPriceAndTotalBeforeCheckout() {
        UserRepository users = mock(UserRepository.class); SellersRepository sellers = mock(SellersRepository.class);
        CartItemRepository lines = mock(CartItemRepository.class);
        CartItem line = cart.getItems().get(0); line.setId(8); line.getMenuItem().setPrice(new BigDecimal("12"));
        when(lines.findById(8)).thenReturn(Optional.of(line)); when(carts.save(any())).thenAnswer(c -> c.getArgument(0));
        CartService cartService = new CartService(carts, lines, users, sellers, mock(MenuItemRepository.class));
        var output = cartService.updateQuantityOutput(1, 4, 8, 3);
        assertEquals(new BigDecimal("12"), output.items().get(0).unitPrice()); assertEquals(new BigDecimal("36"), output.totalAmount());
    }
    @Test void facadeReturnsTheOrderIdWithoutRunningASecondCharge() {
        OrderService core = mock(OrderService.class); PaymentService facade = new PaymentService(payments, orders, core);
        Order order = new Order(); order.setId(10); order.setSeller(cart.getSeller());
        Payment payment = new Payment(); payment.setId(8); payment.setOrder(order); payment.setAmount(BigDecimal.TEN); payment.setStatus("SUCCESS");
        when(core.checkout(eq(1), eq(4), any())).thenReturn(order); when(payments.findAllByOrder_Id(10)).thenReturn(List.of(payment));
        var output = facade.payCart(1, 4); assertEquals(10, output.orderId()); assertEquals("SUCCESS", output.status());
        verify(core, times(1)).checkout(eq(1), eq(4), any()); verify(payments, never()).save(any());
    }
}
