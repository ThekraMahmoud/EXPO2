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
class PreviewConfirmationHttpTest {
    @Autowired WebApplicationContext context;
    @Autowired MenuRepository menus;
    @Autowired MenuItemRepository menuItems;
    @MockitoBean AiMenuClient ai;

    @Test void uploadThenConfirmWithoutBodySavesAllAndRejectsClientItemList() throws Exception {
        Sellerers seller = new Sellerers(); seller.setId(7); seller.setStatus("ACTIVE"); seller.setVerification_status("VERIFIED");
        Menu menu = new Menu(); menu.setId(3); menu.setSeller(seller);
        when(menus.findById(3)).thenReturn(Optional.of(menu));
        when(menus.findByIdForImport(3)).thenReturn(Optional.of(menu));
        when(menuItems.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));
        MenuItem existing = new MenuItem(); existing.setName(" tea "); existing.setPrice(BigDecimal.ONE);
        when(menuItems.findAllByMenu_Id(3)).thenReturn(List.of(existing));
        when(ai.analyzePdf(any())).thenReturn(new MenuImportPreviewResponse(true, "SAR", List.of(
                new MenuImportPreviewResponse.Item("Tea", "Tea details", BigDecimal.TEN),
                new MenuImportPreviewResponse.Item("Book", null, null)), List.of()));
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(multipart("/api/v1/menus/3/restaurant/7/import/preview")
                .file(new MockMultipartFile("file", "menu.pdf", "application/pdf", "%PDF-1.7\nexample".getBytes())))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/menus/3/restaurant/7/import/confirm")
                .contentType("application/json").content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());
        String body = mvc.perform(post("/api/v1/menus/3/restaurant/7/import/confirm"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var saved = JsonMapper.builder().build().readTree(body);
        assertEquals(1, saved.size());
        assertEquals("Book", saved.path(0).path("name").asString());
        assertFalse(saved.path(0).path("available").asBoolean());
        assertEquals(0, saved.path(0).path("price").asInt());
        assertEquals(BigDecimal.ONE, existing.getPrice());
        mvc.perform(post("/api/v1/menus/3/restaurant/7/import/confirm"))
                .andExpect(status().isBadRequest());
        verify(menuItems, times(1)).saveAll(anyList());
    }
}

