package com.example.expo;

import com.example.expo.Controller.InvoicesController;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import com.example.expo.Service.InvoicesService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InvoiceAndModelChangesTest {
    @Test void invoiceListUsesResponseDtoWithoutChangingAmountsOrPersistedState() throws Exception {
        InvoicesRepository repository = mock(InvoicesRepository.class);
        InvoicesService service = new InvoicesService(repository, mock(SellersRepository.class));
        Sellerers seller = new Sellerers(); seller.setBusiness_name("Store");
        Order order = new Order(); order.setId(7);
        OrderItem item = new OrderItem(); item.setItemName("Tea"); item.setQuantity(2); item.setUnitPrice(BigDecimal.TEN);
        order.getItems().add(item);
        Invoices invoice = new Invoices(); invoice.setId(3); invoice.setInvoice_number("INV-3");
        invoice.setSeller(seller); invoice.setOrder(order); invoice.setStatus("PAID");
        invoice.setSubtotal(new BigDecimal("20.00")); invoice.setTax_amount(BigDecimal.ZERO); invoice.setTotal_amount(new BigDecimal("20.00"));
        when(repository.findAll()).thenReturn(List.of(invoice));
        var mvc = MockMvcBuilders.standaloneSetup(new InvoicesController(service)).build();
        mvc.perform(get("/api/v1/invoices/get")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceId").value(3))
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-3"))
                .andExpect(jsonPath("$[0].orderId").value(7))
                .andExpect(jsonPath("$[0].sellerName").value("Store"))
                .andExpect(jsonPath("$[0].items[0].subtotal").value(20))
                .andExpect(jsonPath("$[0].totalAmount").value(20))
                .andExpect(jsonPath("$[0].seller").doesNotExist());
        assertEquals("PAID", invoice.getStatus()); assertEquals(new BigDecimal("20.00"), invoice.getTotal_amount());
        verify(repository, never()).save(any());
    }
    @Test void invoiceNotLinkedToOrderStillAppearsWithEmptyItems() {
        InvoicesRepository repository = mock(InvoicesRepository.class);
        InvoicesService service = new InvoicesService(repository, mock(SellersRepository.class));
        Sellerers seller = new Sellerers(); seller.setBusiness_name("Store");
        Invoices invoice = new Invoices(); invoice.setSeller(seller); invoice.setStatus("PENDING");
        when(repository.findAll()).thenReturn(List.of(invoice));
        var response = service.getAll().get(0);
        assertNull(response.orderId()); assertTrue(response.items().isEmpty()); assertEquals("PENDING", response.status());
    }
    @Test void caregiverIsAnAllowedRoleWhileInvalidRoleRemainsRejected() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator(); User user = new User();
            for (String role : List.of("USER", "ADMIN", "CAREGIVER")) {
                user.setRole(role); assertTrue(validator.validateProperty(user, "role").isEmpty());
            }
            user.setRole("INVALID"); assertFalse(validator.validateProperty(user, "role").isEmpty());
        }
    }
    @Test void cartItemJsonOmitsBackReferenceAndPreservesSubtotalAndJpaRelationship() throws Exception {
        Cart cart = new Cart(); cart.setId(4);
        CartItem item = new CartItem(); item.setId(2); item.setCart(cart); item.setQuantity(2); item.setUnitPrice(BigDecimal.TEN);
        cart.getItems().add(item);
        var json = JsonMapper.builder().build().readTree(JsonMapper.builder().build().writeValueAsString(cart));
        assertEquals(1, json.path("items").size());
        assertFalse(json.path("items").path(0).has("cart"));
        assertEquals(20, json.path("items").path(0).path("subtotal").asInt());
        assertSame(cart, item.getCart());
    }
}
