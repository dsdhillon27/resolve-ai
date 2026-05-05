package com.dsd.resolveai.enums;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RouteDecision(
        @JsonPropertyDescription("Think step-by-step. Analyze the user's intent.")
        String reasoning,

        @JsonPropertyDescription("The final selected agent from the provided list.")
        AgentRoute route
) {}

