package com.dsd.resolveai.tools;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentTools {

    private final IncidentService incidentService;

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
        Search, filter, or look up existing incidents. 
        MUST be used before creating a new incident to check for duplicates!
        Can find incidents by exact status/severity, or semantically search using a keyword.
    """)
    public List<IncidentResponse> searchIncidents(
            @ToolParam(description = "Exact UUID if known. Otherwise leave empty.") String id,
            @ToolParam(description = "Exact status: OPEN, IN_PROGRESS, RESOLVED, CLOSED") String status,
            @ToolParam(description = "Exact severity: LOW, MEDIUM, HIGH, CRITICAL") String severity,
            @ToolParam(description = "Keyword to semantically search incident descriptions, make sure the keyword covers the key details") String keyword,
            @ToolParam(description = "The exact database field to sort by (e.g., createdAt, status).") String sortProperty,
            @ToolParam(description = "The sort direction: ASC or DESC. Default is DESC.") String sortDirection,
            @ToolParam(description = "Max number of results to return (Default 10)") Integer limit
    ) {
        UUID uuid = (id != null && !id.trim().isEmpty()) ? UUID.fromString(id) : null;
        return incidentService.searchIncidents(uuid, status, severity, keyword, sortProperty, sortDirection, limit);
    }

}
