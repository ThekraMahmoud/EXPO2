package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.*;
import com.example.expo.Model.*;
import com.example.expo.Repository.ParamedicRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class PublicParamedicRequestService {
    private final UserHealthCardService cards;
    private final ParamedicRequestService assistance;
    private final ParamedicRequestRepository requests;

    private UserHealthCard card(String token) {
        UserHealthCard card = cards.getHealthCardByToken(token);
        if (card.getUser() == null || !"USER".equals(card.getUser().getRole())
            || !"ACTIVE".equals(card.getUser().getStatus()))
            throw new ApiException("البطاقة غير متاحة لطلب المساعدة");
        return card;
    }
    // Token lookup happens before the patient lock; fresh reads prevent a stale open-request check.
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PublicParamedicRequestStatus create(String token, PublicParamedicRequestInput input) {
        // Resolve the patient from the token, ignoring caller-supplied account IDs.
        UserHealthCard card = card(token);
        ParamedicRequest request = assistance.create(card.getUser().getId(),
            new ParamedicRequestInput(input.locationDescription(), input.note()));
        request.setReporterName(input.reporterName().trim());
        request.setReporterPhone(input.reporterPhone().trim());
        return view(requests.save(request));
    }
    @Transactional(readOnly = true)
    public PublicParamedicRequestStatus status(String token) {
        Integer userId = card(token).getUser().getId();
        return requests.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(userId, List.of("PENDING", "ACCEPTED"))
            .map(this::view).orElse(new PublicParamedicRequestStatus(null, "NONE", null, null));
    }
    private PublicParamedicRequestStatus view(ParamedicRequest request) {
        return new PublicParamedicRequestStatus(request.getId(), request.getStatus(), request.getClinicName(), request.getCreatedAt());
    }
}

