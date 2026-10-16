package com.example.expo.DTO;

import java.time.LocalDateTime;
import java.util.Map;

/** The request's patient health fields, without account details or sharing tokens. */
public record ClinicRequestHealthCardResponse(
    Integer requestId, String visitorName, boolean available,
    LocalDateTime updatedAt, Map<String, Object> fields
) {}
