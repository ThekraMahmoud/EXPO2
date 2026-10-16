package com.example.expo.Service;
import com.example.expo.Api.ApiException;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Repository.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Service @RequiredArgsConstructor
public class HealthCardPatchService {
    private final UserHealthCardRepository cards;
    private final UserRepository users;
    private final ObjectMapper mapper;
    private final Validator validator;
    private static final Set<String> EDITABLE = Set.of("bloodType", "dateOfBirth", "biologicalSex", "preferredLanguage", "countryCode",
            "allergies", "foodAllergies", "medicationAllergies", "otherSensitivities", "allergyItems", "chronicDiseases",
            "previousDiseases", "surgeries", "hospitalizations", "familyMedicalHistory", "chronicDiseaseItems", "currentMedications",
            "medicationDosages", "medicalDevicesAndImplants", "phobias", "phobiaItems", "disabilities", "communicationNeeds",
            "dietaryRestrictions", "mobilityNeeds", "emergencyInstructions", "treatingPhysician", "additionalMedicalNotes",
            "emergencyContactName", "emergencyContactPhone", "emergencyContactRelationship", "notes");

    @Transactional
    public void patch(Integer userId, Integer cardId, Map<String, Object> fields) {
        if (fields == null || !EDITABLE.containsAll(fields.keySet())) throw new ApiException("Unsupported health card fields");
        if (users.findUserById(userId) == null) throw new ApiException("User not found");
        UserHealthCard card = cards.findByUserIdForQr(userId).orElseThrow(() -> new ApiException("Health card not found"));
        if (!card.getId().equals(cardId)) throw new ApiException("Health card does not belong to this user");
        if (fields.isEmpty()) return;
        try {
            // نتحقق من نسخة أولًا؛ إغفال حقل لا يمسح قيمته، وnull الصريح يمسح الحقل المسموح.
            UserHealthCard candidate = mapper.readValue(mapper.writeValueAsString(card), UserHealthCard.class);
            candidate.setUser(card.getUser()); candidate.setPublicToken(card.getPublicToken());
            mapper.readerForUpdating(candidate).readValue(mapper.writeValueAsString(fields));
            normalizeChanged(candidate, fields.keySet());
            if (!validator.validate(candidate).isEmpty()) throw new ApiException("Invalid health card fields");
            mapper.readerForUpdating(card).readValue(mapper.writeValueAsString(fields));
            normalizeChanged(card, fields.keySet());
            cards.save(card);
        } catch (ApiException exception) { throw exception; }
        catch (Exception exception) { throw new ApiException("Invalid health card field types"); }
    }

    static void normalize(UserHealthCard card) {
        card.setAllergyItems(clean(card.getAllergyItems()));
        card.setChronicDiseaseItems(clean(card.getChronicDiseaseItems()));
        card.setPhobiaItems(clean(card.getPhobiaItems()));
        // لا نغيّر دولة قديمة مفقودة أثناء PATCH لحقول أخرى.
        if (card.getCountryCode() != null) card.setCountryCode(card.getCountryCode().trim().toUpperCase(Locale.ROOT));
    }
    private static Set<String> clean(Set<String> values) {
        Set<String> result = new LinkedHashSet<>();
        if (values != null) for (String value : values) if (value != null && !value.isBlank()) result.add(value.trim());
        return result;
    }

    private static void normalizeChanged(UserHealthCard card, Set<String> fields) {
        if (fields.contains("allergyItems")) card.setAllergyItems(clean(card.getAllergyItems()));
        if (fields.contains("chronicDiseaseItems")) card.setChronicDiseaseItems(clean(card.getChronicDiseaseItems()));
        if (fields.contains("phobiaItems")) card.setPhobiaItems(clean(card.getPhobiaItems()));
        if (fields.contains("countryCode") && card.getCountryCode() != null)
            card.setCountryCode(card.getCountryCode().trim().toUpperCase(Locale.ROOT));
    }

}
