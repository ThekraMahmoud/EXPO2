package com.example.expo.DTO;

import java.time.LocalDateTime;

/** A public response never includes patient IDs, contact details or other reports. */
public record PublicParamedicRequestStatus(
    Integer requestId, String status, String clinicName, LocalDateTime createdAt
) {}
