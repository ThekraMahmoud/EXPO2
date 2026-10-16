package com.example.expo;

import com.example.expo.Advice.ControllerAdvice;
import com.example.expo.Controller.*;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Service.UserHealthCardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class HealthCardQrHttpTest {
    final UserHealthCardService service = mock(UserHealthCardService.class);
    final String token = "a".repeat(32);
    MockMvc mvc(String base) {
        return MockMvcBuilders.standaloneSetup(new HealthCardPublicController(service, base),
                new UserHealthCardController(service)).setControllerAdvice(new ControllerAdvice()).build();
    }
    UserHealthCard card() {
        UserHealthCard card = new UserHealthCard(); card.setPublicToken(token); card.setBloodType("O+"); return card;
    }
    @Test void qrUsesRequestHostAndLocalLibraryAndIsNotCached() throws Exception {
        when(service.getHealthCardForQr(1)).thenReturn(card());
        mvc("").perform(get("http://localhost:8080/api/v1/health-cards/qr/1"))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(content().string(containsString("http://localhost:8080/api/v1/health-cards/public/" + token)))
                .andExpect(content().string(containsString("http://localhost:8080/health-card/qrcode.min.js")))
                .andExpect(content().string(not(containsString("cdnjs"))));
    }
    @Test void phoneOrDeploymentUrlCanBeConfiguredWithoutChangingCode() throws Exception {
        when(service.getHealthCardForQr(1)).thenReturn(card());
        mvc("https://expo.example.com/").perform(get("/api/v1/health-cards/qr/1"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("https://expo.example.com/api/v1/health-cards/public/" + token)));
    }
    @Test void invalidBaseUrlCannotBecomeExecutableHtml() throws Exception {
        when(service.getHealthCardForQr(1)).thenReturn(card());
        mvc("javascript:alert(1)").perform(get("/api/v1/health-cards/qr/1")).andExpect(status().isBadRequest());
    }
    @Test void publicPageEscapesExistingMedicalFieldsAndDoesNotExposeUserOrInternalIds() throws Exception {
        UserHealthCard card = card(); card.setId(5); card.setAllergies("<script>alert('x')</script>");
        card.setEmergencyContactName("<b>Contact</b>"); card.setNotes("Old notes");
        when(service.getHealthCardByToken(token)).thenReturn(card);
        String html = mvc("").perform(get("/api/v1/health-cards/public/" + token)).andExpect(status().isOk())
                .andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("O+")); assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("<script>alert")); assertTrue(html.contains("Old notes"));
        assertFalse(html.contains("publicToken")); assertFalse(html.contains("userId"));
    }
    @Test void missingTokenReturnsClearErrorAndEmptyFieldsAreOmitted() throws Exception {
        when(service.getHealthCardByToken("bad")).thenThrow(new com.example.expo.Api.ApiException("Health card not found"));
        mvc("").perform(get("/api/v1/health-cards/public/bad")).andExpect(status().isBadRequest());
        when(service.getHealthCardByToken(token)).thenReturn(new UserHealthCard());
        String html = mvc("").perform(get("/api/v1/health-cards/public/" + token)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(html.contains("<section>"));
    }
    @Test void existingListAddUpdateDeleteRoutesRemainAvailable() throws Exception {
        var mvc = mvc("");
        mvc.perform(get("/api/v1/health-cards/getAllHealthCards")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/health-cards/addHealthCard/1").contentType("application/json").content("{\"bloodType\":\"O+\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/health-cards/updateHealthCard/1/5").contentType("application/json").content("{\"bloodType\":\"AB+\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v1/health-cards/deleteHealthCard/1/5")).andExpect(status().isOk());
        verify(service).updateHealthCard(eq(1), eq(5), any());
    }
}
