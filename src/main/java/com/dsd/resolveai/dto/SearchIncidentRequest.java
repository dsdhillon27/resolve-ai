package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record SearchIncidentRequest(
        List<IncidentFilter> filters,
        String keyword,
        String sortProperty,
        String sortDirection,
        Integer limit
) { }
