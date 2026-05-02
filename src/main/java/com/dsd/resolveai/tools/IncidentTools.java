package com.dsd.resolveai.tools;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.service.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IncidentTools {

    private final IncidentService incidentService;
    private final ObjectMapper objectMapper;

    @Tool(description = """
        Create a new incident in the Incident table 
        when user reports a new issue and returns 
        created incident details in the response
    """)
    public IncidentResponse createIncident(
            @ToolParam(description = "Title of the incident") String title,
            @ToolParam(description = "Description of the incident") String description,
            @ToolParam(description = "Severity level: LOW, MEDIUM, HIGH") String severity,
            @ToolParam(description = "Assignee of the ticket, if not mentioned keep it null") String assignee
    ){
        CreateIncidentRequest request = new CreateIncidentRequest(
                title, description, IncidentSeverity.valueOf(severity.toUpperCase()), assignee);
        IncidentResponse response = incidentService.createIncident(request);

        return response;
    }

    @Tool(description = """
        Search the incidents table dynamically. 
        You MUST call 'getSchema' tool first to understand the exact column names available!
    """)
    public List<IncidentResponse> searchIncidents(
            @ToolParam(description = "A JSON object where keys are EXACT database columns and values are the search terms. Example: '{\"status\": \"OPEN\", \"assignee\": \"Rahul\"}'. Only include fields you want to filter by.")
            String exactFiltersJson,
            @ToolParam(description = "Natural language keyword to semantically search incident descriptions via Vector DB.")
            String keyword,
            @ToolParam(description = "The database field to sort by.") String sortProperty,
            @ToolParam(description = "Sort direction: ASC or DESC. Default is DESC.") String sortDirection,
            @ToolParam(description = "Max results (Default 10).") Integer limit
    ) {
        try {
            Incident probe = null;
            if (exactFiltersJson != null && !exactFiltersJson.trim().isEmpty()) {
                probe = objectMapper.readValue(exactFiltersJson, Incident.class);
            }

            return incidentService.dynamicSearch(probe, keyword, sortProperty, sortDirection, limit);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse filters. Ensure your JSON matches the schema.", e);
        }
    }

}
