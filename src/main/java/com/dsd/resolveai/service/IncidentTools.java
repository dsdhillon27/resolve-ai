package com.dsd.resolveai.service;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.enums.IncidentSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncidentTools {

    private final IncidentService incidentService;

    @Tool(description = "Create a new incident in the Incident table when user" +
            "reports a new issue")
    public String createIncident(
            @ToolParam(description = "Title of the incident") String title,
            @ToolParam(description = "Description of the incident") String description,
            @ToolParam(description = "Severity level: LOW, MEDIUM, HIGH") String severity
    ){
        CreateIncidentRequest request = new CreateIncidentRequest(
                title, description, IncidentSeverity.valueOf(severity.toUpperCase()));
        IncidentResponse response = incidentService.createIncident(request);

        return "Successfully created incident with ID: " + response.id();
    }
}
