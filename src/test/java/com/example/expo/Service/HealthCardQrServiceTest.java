package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCardQrServiceTest {
    final UserHealthCardRepository cards = mock(UserHealthCardRepository.class);
    final UserRepository users = mock(UserRepository.class);
    final UserHealthCardService service = new UserHealthCardService(cards, users);
    User user;
    @BeforeEach void setup() {
        user = new User(); user.setId(1);
        when(users.findUserById(1)).thenReturn(user);
    }
    @Test void serverIssuesDifferentTokensForNewCardsInsteadOfTrustingInput() {
        UserHealthCard first = new UserHealthCard(); first.setPublicToken("client-token");
        UserHealthCard second = new UserHealthCard(); second.setPublicToken("client-token");
        service.addHealthCard(1, first); service.addHealthCard(1, second);
        assertTrue(first.getPublicToken().matches("[a-f0-9]{32}"));
        assertNotEquals(first.getPublicToken(), second.getPublicToken());
        assertSame(user, first.getUser());
    }
    @Test void legacyCardGetsOneStableTokenAndPreservesMedicalFields() {
        UserHealthCard card = new UserHealthCard(); card.setBloodType("O+"); card.setNotes("Existing notes");
        when(cards.findByUserIdForQr(1)).thenReturn(Optional.of(card));
        assertSame(card, service.getHealthCardForQr(1));
        String token = card.getPublicToken();
        assertTrue(token.matches("[a-f0-9]{32}"));
        assertSame(card, service.getHealthCardForQr(1)); assertEquals(token, card.getPublicToken());
        assertEquals("O+", card.getBloodType()); assertEquals("Existing notes", card.getNotes());
        verify(cards, times(1)).save(card);
    }
    @Test void tokenIssuanceRequiresAnExistingUserAndExistingCard() {
        assertThrows(ApiException.class, () -> service.getHealthCardForQr(99));
        when(cards.findByUserIdForQr(1)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.getHealthCardForQr(1));
        verify(cards, never()).save(any());
    }
    @Test void publicLookupUsesUnpredictableTokenInsteadOfNumericUserOrCardId() {
        String token = "a".repeat(32); UserHealthCard card = new UserHealthCard();
        when(cards.findByPublicToken(token)).thenReturn(Optional.of(card));
        assertSame(card, service.getHealthCardByToken(token));
        assertThrows(ApiException.class, () -> service.getHealthCardByToken("1"));
        assertThrows(ApiException.class, () -> service.getHealthCardByToken("b".repeat(32)));
        verify(cards, never()).findByPublicToken("1");
    }
    @Test void existingUpdateCannotReplaceSharingToken() {
        UserHealthCard old = new UserHealthCard(); old.setUser(user); old.setPublicToken("a".repeat(32));
        when(cards.findUserHealthCardById(5)).thenReturn(old);
        UserHealthCard input = new UserHealthCard(); input.setPublicToken("forged"); input.setBloodType("AB+");
        service.updateHealthCard(1, 5, input);
        assertEquals("a".repeat(32), old.getPublicToken()); assertEquals("AB+", old.getBloodType());
    }
    @Test void sharingTokenIsNeitherReadFromNorWrittenToNormalJson() throws Exception {
        var mapper = JsonMapper.builder().build();
        UserHealthCard card = new UserHealthCard(); card.setPublicToken("secret"); card.setBloodType("A+");
        assertFalse(mapper.readTree(mapper.writeValueAsString(card)).has("publicToken"));
        assertNull(mapper.readValue("{\"publicToken\":\"forged\",\"bloodType\":\"O+\"}", UserHealthCard.class).getPublicToken());
    }
}
