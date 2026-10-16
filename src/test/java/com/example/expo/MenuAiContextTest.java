package com.example.expo;

import com.example.expo.Repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import com.example.expo.Model.Menu;
import com.example.expo.Model.Sellerers;
import com.example.expo.Client.AiMenuClient;
import com.example.expo.DTO.MenuImportPreviewResponse;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import com.example.expo.Model.MenuItem;
import java.math.BigDecimal;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// نشغل سياق التطبيق كاملًا مع بدائل للريبو: لا نتصل بقاعدة المستخدم ولا بخدمة OpenRouter.
@SpringBootTest(properties = {
        "app.menu-import.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
})
@MockitoBean(types = {MenuRepository.class, MenuItemRepository.class, SellersRepository.class,
        CartRepository.class, CartItemRepository.class, OrderRepository.class, InvoicesRepository.class,
        UserRepository.class, UserWalletsRepository.class, UserWalletTransactionRepository.class,
        SellersWalletsRepository.class, SellerWallerTransactionRepository.class,
        UserDocumentRepository.class, UserHealthCardRepository.class, VisitedPlaceRepository.class})
@MockitoBean(types = {DestinationRepository.class, MapNodeRepository.class, MapEdgesRepository.class,
        UserDestinationProgressRepository.class, ClinicBranchRepository.class, com.example.expo.Repository.ParamedicRequestRepository.class, PaymentRepository.class,
        com.example.expo.Config.AdminInitializer.class})
class MenuAiContextTest {
    @Autowired WebApplicationContext context;
    @Autowired MenuRepository menus;
    @Autowired MenuItemRepository menuItems;
    @Autowired SellersRepository sellers;

    @Test void originalRoutesStillLoadWithAiDisabled() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(get("/api/v1/menus/restaurant/7")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/orders/user/7")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/seller/get")).andExpect(status().isOk());
    }

    @Test void newRoutesValidateNestedItemsAndMissingUpload() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(multipart("/api/v1/menus/3/restaurant/7/import/preview"))
                .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Missing upload field: file")));
        mvc.perform(post("/api/v1/menus/3/restaurant/7/import/confirm")
                .contentType("application/json").content("{\"items\":[{\"name\":\"Tea\",\"price\":null}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test void pdfUploadReachesPreviewAndDisabledAiReturnsClearMessage() throws Exception {
        Sellerers seller = new Sellerers(); seller.setId(7); seller.setStatus("ACTIVE"); seller.setVerification_status("VERIFIED");
        Menu menu = new Menu(); menu.setId(3); menu.setSeller(seller);
        when(menus.findById(3)).thenReturn(Optional.of(menu));
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(multipart("/api/v1/menus/3/restaurant/7/import/preview")
                .file(new MockMultipartFile("file", "menu.pdf", "application/pdf", "%PDF-1.7\nexample".getBytes())))
                .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Menu AI is disabled")));
    }

    @Test void inactiveRoutesAndPriceCorrectionWorkThroughHttp() throws Exception {
        Sellerers seller = new Sellerers(); seller.setId(7);
        Menu menu = new Menu(); menu.setId(3); menu.setSeller(seller);
        MenuItem product = new MenuItem(); product.setId(8); product.setMenu(menu);
        product.setName("Laptop X"); product.setPrice(BigDecimal.ZERO); product.setAvailable(false);
        when(sellers.findById(7)).thenReturn(Optional.of(seller));
        when(menus.findById(3)).thenReturn(Optional.of(menu));
        when(menuItems.findById(8)).thenReturn(Optional.of(product));
        when(menuItems.findAllByMenu_IdAndAvailableFalse(3)).thenReturn(List.of(product));
        when(menuItems.findAllByMenu_Seller_IdAndAvailableFalse(7)).thenReturn(List.of(product));
        when(menuItems.save(any())).thenAnswer(call -> call.getArgument(0));
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(get("/api/v1/menus/restaurant/7/items/inactive")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/menus/3/restaurant/7/items/inactive")).andExpect(status().isOk());
        var zero = mvc.perform(put("/api/v1/menus/items/8/restaurant/7").contentType("application/json")
                .content("{\"name\":\"Laptop X\",\"price\":0,\"available\":true}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(JsonMapper.builder().build().readTree(zero).path("available").asBoolean());
        var priced = mvc.perform(put("/api/v1/menus/items/8/restaurant/7").contentType("application/json")
                .content("{\"name\":\"Laptop X\",\"price\":3299,\"description\":\"16GB RAM\",\"available\":true}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(JsonMapper.builder().build().readTree(priced).path("available").asBoolean());
    }
}

