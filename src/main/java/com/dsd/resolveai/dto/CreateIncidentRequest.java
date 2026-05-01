package com.dsd.resolveai.dto;

import com.dsd.resolveai.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIncidentRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Severity is required") IncidentSeverity severity,
        String assignee
) { }
