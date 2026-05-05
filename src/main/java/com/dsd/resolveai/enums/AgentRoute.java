package com.dsd.resolveai.enums;

public enum AgentRoute {
    SRE_AGENT("Handles database crashes, incidents, and runbook tasks."),
    GENERAL_QA("Handles greetings, pleasantries, and general IT questions.");

    private final String description;

    AgentRoute(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

