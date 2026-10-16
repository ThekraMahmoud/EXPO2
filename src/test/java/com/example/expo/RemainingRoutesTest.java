package com.example.expo;
import com.example.expo.Advice.ControllerAdvice;
import com.example.expo.Controller.*;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import com.example.expo.Service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RemainingRoutesTest {
    @Test void adminApprovalRequiresAnActiveAdminIdOnTheExistingPaths() throws Exception {
        UserRepository users = mock(UserRepository.class); SellersRepository sellers = mock(SellersRepository.class);
        AdminSellerService service = new AdminSellerService(mock(ClinicBranchRepository.class), sellers, users);
        User admin = new User(); admin.setRole("ADMIN"); admin.setStatus("ACTIVE");
        User visitor = new User(); visitor.setRole("USER"); visitor.setStatus("ACTIVE");
        when(users.findById(9)).thenReturn(Optional.of(admin)); when(users.findById(1)).thenReturn(Optional.of(visitor));
        var mvc = MockMvcBuilders.standaloneSetup(new AdminSellerController(service)).setControllerAdvice(new ControllerAdvice()).build();
        mvc.perform(get("/api/v1/admin/restaurants/pending")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/restaurants/pending").param("adminId", "1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/admin/restaurants/pending").param("adminId", "9")).andExpect(status().isOk());
    }
    @Test void cartRoutesReturnEditingIdsTotalAndStatus() throws Exception {
        CartService service = mock(CartService.class);
        when(service.addItemOutput(eq(1), eq(7), any())).thenReturn(new CartResponse("Added", 4, "Store",
                List.of(new CartResponse.Item(8, 3, "Book", 2, BigDecimal.TEN, new BigDecimal("20"))), new BigDecimal("20"), "PENDING"));
        var mvc = MockMvcBuilders.standaloneSetup(new CartController(service)).build();
        mvc.perform(post("/api/v1/carts/user/1/restaurant/7/items").contentType("application/json").content("{\"menuItemId\":3,\"quantity\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].cartItemId").value(8))
                .andExpect(jsonPath("$.items[0].menuItemId").value(3)).andExpect(jsonPath("$.totalAmount").value(20));
    }
    @Test void cartPaymentEndpointExistsAndOrderOutputDoesNotContainUserAccount() throws Exception {
        PaymentService payments = mock(PaymentService.class); OrderService orders = mock(OrderService.class);
        when(payments.payCart(1, 4)).thenReturn(new PaymentResponse(8, "Store", List.of(), BigDecimal.TEN, "SUCCESS"));
        User user = new User(); user.setPassword("should-not-be-serialized");
        Sellerers seller = new Sellerers(); seller.setBusiness_name("Store");
        Order order = new Order(); order.setId(10); order.setSeller(seller); order.setUser(user); order.setStatus("NEW"); order.setPaymentStatus("PAID");
        when(orders.checkout(eq(1), eq(4), any())).thenReturn(order);
        var mvc = MockMvcBuilders.standaloneSetup(new CartPaymentController(payments), new OrderController(orders)).build();
        mvc.perform(post("/api/v1/payments/cart/4/user/1")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"));
        mvc.perform(post("/api/v1/orders/user/1/checkout/4").contentType("application/json").content("{\"paymentMethod\":\"WALLET\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(10)).andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.user").doesNotExist());
    }
    @Test void newHealthPatchCoexistsWithOldPutWithoutChangingThePublicRoutes() throws Exception {
        UserHealthCardService original = mock(UserHealthCardService.class); HealthCardPatchService patch = mock(HealthCardPatchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserHealthCardController(original), new HealthCardPatchController(patch)).build();
        mvc.perform(patch("/api/v1/health-cards/updateHealthCard/1/5").contentType("application/json").content("{\"notes\":\"Updated\"}"))
                .andExpect(status().isOk());
        verify(patch).patch(1, 5, Map.of("notes", "Updated"));
        mvc.perform(put("/api/v1/health-cards/updateHealthCard/1/5").contentType("application/json").content("{\"bloodType\":\"O+\"}"))
                .andExpect(status().isOk());
        verify(original).updateHealthCard(eq(1), eq(5), any());
    }
}
