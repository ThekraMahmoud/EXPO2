package com.example.expo;

import com.example.expo.Controller.*;
import com.example.expo.Service.*;
import com.example.expo.Client.MoyasarClient;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// اختبارات المسارات مستقلة: لا نشغّل AdminInitializer أو قاعدة البيانات أو أي دفع.
class NewEndpointsOnlyTest {
    @Test void newClinicRoutesPassAdminIdWithoutChangingRestaurantRoutes() throws Exception {
        AdminSellerService service = mock(AdminSellerService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AdminClinicBranchController(service)).build();
        mvc.perform(get("/api/v1/admin/clinic-branches").param("adminId", "9")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/clinic-branches/2").param("adminId", "9")).andExpect(status().isOk());
        String json = "{\"name\":\"Clinic A\",\"zone\":\"A\",\"locationDescription\":\"Entrance\",\"phone\":\"0501234567\",\"email\":\"clinic@example.com\"}";
        mvc.perform(post("/api/v1/admin/clinic-branches").param("adminId", "9").contentType("application/json").content(json)).andExpect(status().isOk());
        mvc.perform(put("/api/v1/admin/clinic-branches/2").param("adminId", "9").contentType("application/json").content(json)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/admin/clinic-branches/2").param("adminId", "9")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/clinic-branches")).andExpect(status().isBadRequest());
        verify(service).getAllClinicBranches(9); verify(service).getClinicBranchById(9, 2);
        verify(service).addClinicBranch(eq(9), any()); verify(service).updateClinicBranch(eq(9), eq(2), any());
        verify(service).deleteClinicBranch(9, 2);
    }

    @Test void paymentReadRoutesCoexistWithExistingMoyasarVerification() throws Exception {
        PaymentService service = mock(PaymentService.class);
        MoyasarClient moyasar = mock(MoyasarClient.class);
        when(moyasar.verifyPayment("ref")).thenReturn("verified");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentReadController(service), new PaymentController(moyasar)).build();
        for (String path : List.of("all", "12", "order/3", "status/SUCCESS")) {
            mvc.perform(get("/api/v1/payments/" + path)).andExpect(status().isOk());
        }
        mvc.perform(get("/api/v1/payments/verify/ref")).andExpect(status().isOk()).andExpect(content().string("verified"));
        verify(service).getAllPayments(); verify(service).getPaymentById(12);
        verify(service).getPaymentsByOrder(3); verify(service).getPaymentsByStatus("SUCCESS");
        verify(service, never()).payCart(any(), any());
    }

    @Test void newSingleCartAndInvoiceRoutesDoNotReplaceExistingListRoutes() throws Exception {
        CartService carts = mock(CartService.class);
        InvoicesService invoices = mock(InvoicesService.class);
        when(carts.getCartById(1, 3)).thenReturn(new CartResponse("Cart", 3, "Store", List.of()));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CartController(carts), new InvoicesController(invoices)).build();
        mvc.perform(get("/api/v1/carts/3/user/1")).andExpect(status().isOk()).andExpect(jsonPath("$.cartId").value(3));
        mvc.perform(get("/api/v1/carts/user/1")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/invoices/4")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/invoices/get")).andExpect(status().isOk());
        verify(carts).getCartById(1, 3); verify(carts).userCartOutputs(1);
        verify(invoices).getById(4); verify(invoices).getAll();
    }

    @Test void orderShortcutsDelegateToCurrentStatusLogicAndOldRouteStillWorks() throws Exception {
        OrderService service = mock(OrderService.class);
        Order order = new Order(); order.setId(2); order.setTotalAmount(BigDecimal.TEN);
        Sellerers seller = new Sellerers(); seller.setBusiness_name("Store"); order.setSeller(seller);
        when(service.updateSellerStatus(eq(7), eq(2), anyString())).thenReturn(order);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(service)).build();
        mvc.perform(get("/api/v1/orders/all")).andExpect(status().isOk());
        String[][] transitions = {{"accept", "ACCEPTED"}, {"prepare", "PREPARING"}, {"ready", "READY"}, {"complete", "COMPLETED"}};
        for (String[] transition : transitions) {
            mvc.perform(patch("/api/v1/orders/2/restaurant/7/" + transition[0]))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(2));
            verify(service).updateSellerStatus(7, 2, transition[1]);
        }
        mvc.perform(patch("/api/v1/orders/2/restaurant/7/status").contentType("application/json")
                .content("{\"status\":\"DELAYED\"}")).andExpect(status().isOk());
        verify(service).updateSellerStatus(7, 2, "DELAYED");
    }

    @Test void menuAdminRouteCoexistsWithCurrentItemOutputAndInactiveRoutes() throws Exception {
        MenuService service = mock(MenuService.class); MenuImportService importer = mock(MenuImportService.class);
        MenuItem item = new MenuItem(); item.setId(8); item.setName("Tea"); item.setPrice(BigDecimal.TEN); item.setAvailable(false);
        when(service.items(1)).thenReturn(List.of(item));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MenuController(service, importer)).build();
        mvc.perform(get("/api/v1/menus/admin/all")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/menus/1/items")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tea")).andExpect(jsonPath("$[0].available").doesNotExist());
        mvc.perform(get("/api/v1/menus/1/restaurant/7/items/inactive")).andExpect(status().isOk());
        verify(service).getAllMenusForAdmin(); verify(service).inactiveItems(7, 1);
    }
}

