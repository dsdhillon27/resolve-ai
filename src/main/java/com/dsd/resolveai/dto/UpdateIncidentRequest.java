package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;

public record UpdateIncidentRequest(
        String title,
        String description,
        IncidentSeverity severity,
        IncidentStatus status
) { }
