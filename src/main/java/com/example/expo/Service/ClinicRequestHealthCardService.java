package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.ClinicRequestHealthCardResponse;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class ClinicRequestHealthCardService {
    private final ParamedicRequestService assistance;
    private final ParamedicRequestRepository requests;
    private final UserHealthCardRepository cards;

    @Transactional(readOnly = true)
    public ClinicRequestHealthCardResponse get(Integer caregiverId, Integer requestId) {
        ClinicBranch clinic = assistance.clinic(caregiverId);
        ParamedicRequest request = requests.findById(requestId)
            .orElseThrow(() -> new ApiException("طلب المساعدة غير موجود"));
        // Pending requests are visible to responding clinics. Once claimed, only its clinic retains access.
        boolean pending = "PENDING".equals(request.getStatus()) && request.getClinicId() == null;
        boolean assigned = Objects.equals(request.getClinicId(), clinic.getId())
            && List.of("ACCEPTED", "COMPLETED").contains(request.getStatus());
        if (!pending && !assigned) throw new ApiException("البطاقة غير متاحة لعيادتك لهذا الطلب");
        UserHealthCard card = cards.findUserHealthCardByUserId(request.getUserId());
        if (card == null) return new ClinicRequestHealthCardResponse(requestId, request.getVisitorName(), false, null, Map.of());
        // Explicit allowlist: never serialize User, wallet, password or the public-sharing token.
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("bloodType", card.getBloodType());
        fields.put("dateOfBirth", card.getDateOfBirth());
        fields.put("biologicalSex", card.getBiologicalSex());
        fields.put("preferredLanguage", card.getPreferredLanguage());
        fields.put("countryCode", card.getCountryCode());
        fields.put("allergies", card.getAllergies());
        fields.put("foodAllergies", card.getFoodAllergies());
        fields.put("medicationAllergies", card.getMedicationAllergies());
        fields.put("otherSensitivities", card.getOtherSensitivities());
        fields.put("allergyItems", card.getAllergyItems() == null ? List.of() : List.copyOf(card.getAllergyItems()));
        fields.put("chronicDiseases", card.getChronicDiseases());
        fields.put("previousDiseases", card.getPreviousDiseases());
        fields.put("surgeries", card.getSurgeries());
        fields.put("hospitalizations", card.getHospitalizations());
        fields.put("familyMedicalHistory", card.getFamilyMedicalHistory());
        fields.put("chronicDiseaseItems", card.getChronicDiseaseItems() == null ? List.of() : List.copyOf(card.getChronicDiseaseItems()));
        fields.put("currentMedications", card.getCurrentMedications());
        fields.put("medicationDosages", card.getMedicationDosages());
        fields.put("medicalDevicesAndImplants", card.getMedicalDevicesAndImplants());
        fields.put("phobias", card.getPhobias());
        fields.put("phobiaItems", card.getPhobiaItems() == null ? List.of() : List.copyOf(card.getPhobiaItems()));
        fields.put("disabilities", card.getDisabilities());
        fields.put("communicationNeeds", card.getCommunicationNeeds());
        fields.put("dietaryRestrictions", card.getDietaryRestrictions());
        fields.put("mobilityNeeds", card.getMobilityNeeds());
        fields.put("emergencyContactName", card.getEmergencyContactName());
        fields.put("emergencyContactPhone", card.getEmergencyContactPhone());
        fields.put("emergencyContactRelationship", card.getEmergencyContactRelationship());
        fields.put("emergencyInstructions", card.getEmergencyInstructions());
        fields.put("treatingPhysician", card.getTreatingPhysician());
        fields.put("additionalMedicalNotes", card.getAdditionalMedicalNotes());
        fields.put("notes", card.getNotes());
        return new ClinicRequestHealthCardResponse(requestId, request.getVisitorName(), true, card.getUpdatedAt(), fields);
    }
}
