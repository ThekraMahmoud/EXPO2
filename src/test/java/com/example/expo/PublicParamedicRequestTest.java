package com.example.expo;

import com.example.expo.Advice.ControllerAdvice;
import com.example.expo.Api.ApiException;
import com.example.expo.Controller.PublicParamedicRequestController;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.ParamedicRequestRepository;
import com.example.expo.Service.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PublicParamedicRequestTest {
    UserHealthCardService cards = mock(UserHealthCardService.class);
    ParamedicRequestService assistance = mock(ParamedicRequestService.class);
    ParamedicRequestRepository requests = mock(ParamedicRequestRepository.class);
    PublicParamedicRequestService service = new PublicParamedicRequestService(cards, assistance, requests);
    String token = "a".repeat(32);
    User user;
    UserHealthCard card;
    @BeforeEach void setup() {
        user = new User();user.setId(7);user.setRole("USER");user.setStatus("ACTIVE");
        card = new UserHealthCard();card.setUser(user);card.setPublicToken(token);
        when(cards.getHealthCardByToken(token)).thenReturn(card);
    }
    @Test void reporterIsSeparateFromPatientAndPublicResponseOmitsPersonalData() {
        ParamedicRequest request = new ParamedicRequest();request.setId(10);request.setUserId(7);request.setVisitorName("المريض");request.setCreatedAt(LocalDateTime.now());
        when(assistance.create(eq(7),any())).thenReturn(request);
        when(requests.save(any())).thenAnswer(i->i.getArgument(0));
        var result = service.create(token,new PublicParamedicRequestInput("المبلغ", "+966551234567", "البوابة",null));
        assertEquals(7,request.getUserId());assertEquals("المريض",request.getVisitorName());
        assertEquals("المبلغ",request.getReporterName());assertEquals("+966551234567",request.getReporterPhone());
        assertEquals(10,result.requestId());assertEquals(4,PublicParamedicRequestStatus.class.getRecordComponents().length);
    }
    @Test void missingOrRevokedTokenCannotCreateARequest() {
        when(cards.getHealthCardByToken(token)).thenThrow(new ApiException("Health card not found"));
        assertThrows(ApiException.class,()->service.create(token,new PublicParamedicRequestInput("اسم","0551234567","موقع",null)));
        verifyNoInteractions(assistance,requests);
    }
    @Test void inactivePatientCannotReceivePublicRequests() {
        user.setStatus("INACTIVE");
        assertThrows(ApiException.class,()->service.create(token,new PublicParamedicRequestInput("اسم","0551234567","موقع",null)));
        verifyNoInteractions(assistance,requests);
    }
    @Test void existingDuplicateProtectionIsUsedForQrAndSelfReports() {
        when(assistance.create(eq(7),any())).thenThrow(new ApiException("Open request already exists"));
        assertThrows(ApiException.class,()->service.create(token,new PublicParamedicRequestInput("اسم","0551234567","موقع",null)));
        verify(requests,never()).save(any());
    }
    @Test void publicStatusOnlyShowsActiveRequestForTokenOwner() {
        when(requests.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(eq(7),any())).thenReturn(Optional.empty());
        assertEquals("NONE",service.status(token).status());
        verify(requests).findFirstByUserIdAndStatusInOrderByCreatedAtDesc(eq(7),any());
    }
    MockMvc mvc(PublicParamedicRequestService mock) {
        return MockMvcBuilders.standaloneSetup(new PublicParamedicRequestController(mock))
            .setControllerAdvice(new ControllerAdvice()).build();
    }
    @Test void publicEndpointRequiresNamePhoneAndLocationAndRejectsMalformedPhone() throws Exception {
        var mock = mock(PublicParamedicRequestService.class);
        for(String body : new String[]{"{}", "{\"reporterName\":\"Name\",\"reporterPhone\":\"bad-phone\",\"locationDescription\":\"Gate\"}",
            "{\"reporterName\":\"Name\",\"reporterPhone\":\"0551234567\",\"locationDescription\":\" \"}"})
            mvc(mock).perform(post("/api/v1/health-cards/public/"+token+"/paramedic-request").contentType("application/json").content(body)).andExpect(status().isBadRequest());
        verifyNoInteractions(mock);
    }
    @Test void noLoginIsRequiredAndResponseIsNotCached() throws Exception {
        var mock = mock(PublicParamedicRequestService.class);
        when(mock.create(eq(token),any())).thenReturn(new PublicParamedicRequestStatus(10,"PENDING",null,LocalDateTime.now()));
        mvc(mock).perform(post("/api/v1/health-cards/public/"+token+"/paramedic-request").contentType("application/json")
            .content("{\"reporterName\":\"Reporter\",\"reporterPhone\":\"٠٥٥١٢٣٤٥٦٧\",\"locationDescription\":\"Gate\"}"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
            .andExpect(jsonPath("$.requestId").value(10)).andExpect(jsonPath("$.userId").doesNotExist())
            .andExpect(jsonPath("$.reporterPhone").doesNotExist());
    }
}
