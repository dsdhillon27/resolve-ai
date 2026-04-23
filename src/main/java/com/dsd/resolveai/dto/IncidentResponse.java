package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
    UUID id,
    String title,
    String description,
    IncidentSeverity severity,
    IncidentStatus status,
    Instant createdAt
) { }
