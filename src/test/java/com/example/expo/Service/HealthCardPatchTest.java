package com.example.expo.Service;
import com.example.expo.Api.ApiException;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCardPatchTest {
    UserHealthCardRepository cards = mock(UserHealthCardRepository.class);
    UserRepository users = mock(UserRepository.class);
    jakarta.validation.ValidatorFactory factory;
    HealthCardPatchService service; UserHealthCard card;
    @BeforeEach void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        service = new HealthCardPatchService(cards, users, JsonMapper.builder().build(), factory.getValidator());
        User user = new User(); user.setId(1); when(users.findUserById(1)).thenReturn(user);
        card = new UserHealthCard(); card.setId(5); card.setUser(user); card.setPublicToken("a".repeat(32));
        card.setBloodType("O+"); card.setCountryCode("US"); card.setNotes("Legacy notes");
        card.getAllergyItems().add("Peanuts"); card.getChronicDiseaseItems().add("Asthma");
        when(cards.findByUserIdForQr(1)).thenReturn(Optional.of(card));
    }
    @AfterEach void close() { factory.close(); }
    @Test void omittedCountryCollectionsAndLegacyFieldsArePreserved() {
        service.patch(1, 5, Map.of("medicationDosages", "Daily dose", "dateOfBirth", "2000-01-02"));
        assertEquals("US", card.getCountryCode()); assertEquals(Set.of("Peanuts"), card.getAllergyItems());
        assertEquals(Set.of("Asthma"), card.getChronicDiseaseItems()); assertEquals("Legacy notes", card.getNotes());
        assertEquals(java.time.LocalDate.of(2000, 1, 2), card.getDateOfBirth()); assertEquals("a".repeat(32), card.getPublicToken());
    }
    @Test void explicitNullAndEmptyCollectionCanClearWithoutResettingOtherFields() {
        Map<String, Object> patch = new HashMap<>(); patch.put("notes", null); patch.put("allergyItems", List.of());
        service.patch(1, 5, patch);
        assertNull(card.getNotes()); assertTrue(card.getAllergyItems().isEmpty());
        assertEquals("US", card.getCountryCode()); assertEquals(Set.of("Asthma"), card.getChronicDiseaseItems());
    }
    @Test void invalidDataOrProtectedFieldsDoNotMutateCard() {
        assertThrows(ApiException.class, () -> service.patch(1, 5, Map.of("bloodType", "INVALID")));
        assertEquals("O+", card.getBloodType());
        assertThrows(ApiException.class, () -> service.patch(1, 5, Map.of("publicToken", "forged")));
        assertThrows(ApiException.class, () -> service.patch(1, 5, Map.of("allergyItems", List.of("x".repeat(201)))));
        verify(cards, never()).save(any());
    }
    @Test void otherUserOrCardCannotBeUpdated() {
        assertThrows(ApiException.class, () -> service.patch(99, 5, Map.of("notes", "changed")));
        assertThrows(ApiException.class, () -> service.patch(1, 99, Map.of("notes", "changed")));
        assertEquals("Legacy notes", card.getNotes()); verify(cards, never()).save(any());
    }
}
