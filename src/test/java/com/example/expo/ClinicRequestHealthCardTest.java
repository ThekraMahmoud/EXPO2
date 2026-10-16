package com.example.expo;

import com.example.expo.Api.ApiException;
import com.example.expo.Advice.ControllerAdvice;
import com.example.expo.Controller.ClinicRequestHealthCardController;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import com.example.expo.Service.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClinicRequestHealthCardTest {
    ParamedicRequestService assistance = mock(ParamedicRequestService.class);
    ParamedicRequestRepository requests = mock(ParamedicRequestRepository.class);
    UserHealthCardRepository cards = mock(UserHealthCardRepository.class);
    ClinicRequestHealthCardService service = new ClinicRequestHealthCardService(assistance, requests, cards);
    ParamedicRequest request;
    UserHealthCard card;
    @BeforeEach void setup() {
        ClinicBranch clinic = new ClinicBranch();clinic.setId(4);
        when(assistance.clinic(2)).thenReturn(clinic);
        request = new ParamedicRequest();request.setId(10);request.setUserId(7);request.setVisitorName("المريض");
        when(requests.findById(10)).thenReturn(Optional.of(request));
        card = new UserHealthCard();card.setBloodType("O+");card.setMedicationDosages("جرعة مسجلة");
        card.getAllergyItems().add("حساسية مسجلة");card.setPublicToken("a".repeat(32));
        when(cards.findUserHealthCardByUserId(7)).thenReturn(card);
    }
    @Test void pendingRequestOpensPatientHealthFieldsWithoutSharingTokenOrAccount() {
        var response = service.get(2,10);
        assertTrue(response.available());assertEquals("O+",response.fields().get("bloodType"));
        assertEquals("جرعة مسجلة",response.fields().get("medicationDosages"));
        assertEquals(List.of("حساسية مسجلة"),response.fields().get("allergyItems"));
        assertFalse(response.fields().containsKey("publicToken"));assertFalse(response.fields().containsKey("user"));
        assertFalse(response.fields().containsKey("password"));verify(cards).findUserHealthCardByUserId(7);
    }
    @Test void ownClinicCanReadAcceptedAndCompletedRequests() {
        request.setClinicId(4);
        for(String status:List.of("ACCEPTED","COMPLETED")) {
            request.setStatus(status);assertTrue(service.get(2,10).available());
        }
    }
    @Test void otherClinicCannotReadClaimedPatientCard() {
        request.setStatus("ACCEPTED");request.setClinicId(8);
        assertThrows(ApiException.class,()->service.get(2,10));verifyNoInteractions(cards);
    }
    @Test void cancelledRequestCannotBeUsedToReadPatientCard() {
        request.setStatus("CANCELLED");assertThrows(ApiException.class,()->service.get(2,10));verifyNoInteractions(cards);
    }
    @Test void missingCardIsReportedWithoutCreatingOne() {
        when(cards.findUserHealthCardByUserId(7)).thenReturn(null);
        var response=service.get(2,10);assertFalse(response.available());assertTrue(response.fields().isEmpty());
        verify(cards,never()).save(any());
    }
    @Test void visitorOrUnassignedCaregiverIsRejectedBeforeReadingAnyRequest() {
        when(assistance.clinic(2)).thenThrow(new ApiException("Not authorized"));
        assertThrows(ApiException.class,()->service.get(2,10));verifyNoInteractions(requests,cards);
    }
    @Test void httpResponseIsNotCachedAndDoesNotExposeSharingToken() throws Exception {
        MockMvcBuilders.standaloneSetup(new ClinicRequestHealthCardController(service))
            .setControllerAdvice(new ControllerAdvice()).build()
            .perform(get("/api/v1/paramedic-requests/10/health-card?caregiverId=2"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
            .andExpect(jsonPath("$.fields.bloodType").value("O+"))
            .andExpect(jsonPath("$.fields.publicToken").doesNotExist())
            .andExpect(jsonPath("$.user").doesNotExist());
    }
}
