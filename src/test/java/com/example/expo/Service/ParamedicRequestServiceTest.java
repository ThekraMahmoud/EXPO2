package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.ParamedicRequestInput;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ParamedicRequestServiceTest {
    final ParamedicRequestRepository requests = mock(ParamedicRequestRepository.class);
    final UserRepository users = mock(UserRepository.class);
    final ClinicBranchRepository clinics = mock(ClinicBranchRepository.class);
    final ParamedicRequestService service = new ParamedicRequestService(requests, users, clinics);
    @BeforeEach void setup() {
        User visitor = new User();visitor.setId(1);visitor.setRole("USER");visitor.setStatus("ACTIVE");visitor.setUsername("زائر");
        when(users.findById(1)).thenReturn(Optional.of(visitor));
        when(users.findForParamedicRequest(1)).thenReturn(Optional.of(visitor));
        User caregiver = new User();caregiver.setId(2);caregiver.setRole("CAREGIVER");caregiver.setStatus("ACTIVE");
        when(users.findById(2)).thenReturn(Optional.of(caregiver));
        ClinicBranch branch = new ClinicBranch();branch.setId(4);branch.setName("عيادة");branch.setCaregiverId(2);
        when(clinics.findByCaregiverId(2)).thenReturn(Optional.of(branch));
        when(requests.save(any())).thenAnswer(i -> i.getArgument(0));
    }
    @Test void keepsManualLocationWithoutRequiringOrCopyingMedicalCard() {
        ParamedicRequest r = service.create(1, new ParamedicRequestInput("  بجوار النافورة  ", null));
        assertEquals("بجوار النافورة",r.getLocationDescription());assertEquals("PENDING",r.getStatus());assertNull(r.getClinicId());
    }
    @Test void refusesBlankLocationAndDuplicateOpenRequest() {
        assertThrows(ApiException.class, () -> service.create(1,new ParamedicRequestInput("  ",null)));
        when(requests.existsByUserIdAndStatusIn(eq(1), any())).thenReturn(true);
        assertThrows(ApiException.class, () -> service.create(1,new ParamedicRequestInput("بوابة",null)));
        verify(requests,never()).save(any());
    }
    @Test void visitorCannotReadClinicInbox() { assertThrows(ApiException.class, () -> service.inbox(1)); }
    @Test void unassignedCaregiverCannotAccept() {
        when(clinics.findByCaregiverId(2)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.accept(2,10));verify(requests,never()).claim(any(),any(),any(),any());
    }
    @Test void losingClaimDoesNotOverwriteWinningClinic() {
        when(requests.claim(eq(10),eq(4),any(),any())).thenReturn(0);
        assertThrows(ApiException.class, () -> service.accept(2,10));verify(requests,never()).save(any());
    }
    @Test void cannotCancelAcceptedOrOtherVisitorsRequest() {
        when(requests.cancel(eq(10),eq(1),any())).thenReturn(0);
        assertThrows(ApiException.class, () -> service.cancel(1,10));
    }
    @Test void onlyAssignedClinicCanCompleteItsAcceptedRequest() {
        when(requests.complete(eq(10),eq(4),any())).thenReturn(0);
        assertThrows(ApiException.class, () -> service.complete(2,10));
    }
}
