package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SearchIncidentRequest(

        IncidentStatus status,

        IncidentSeverity severity,

        String assignee,

        @JsonPropertyDescription("Natural language keyword to semantically search incident descriptions.")
        String keyword,

        @JsonPropertyDescription("The database field to sort by.")
        String sortProperty,

        @JsonPropertyDescription("Sort direction: ASC or DESC. Default is DESC.")
        String sortDirection,

        @JsonPropertyDescription("Max results (Default 10).")
        Integer limit
) {}
