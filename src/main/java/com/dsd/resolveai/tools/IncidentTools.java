package com.dsd.resolveai.tools;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.dto.SearchIncidentRequest;
import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;
import com.dsd.resolveai.service.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        Create a new incident in the Incident table. 
        If forceCreate is false or not provided, the system will check for semantic duplicates first.
        If the system returns a warning about duplicates, you MUST STOP and ask the user for confirmation before retrying with forceCreate=true.
    """)
    public Object createIncident(
            @ToolParam(description = "Title of the incident") String title,
            @ToolParam(description = "Description of the incident") String description,
            @ToolParam(description = "Severity level: LOW, MEDIUM, HIGH") String severity,
            @ToolParam(description = "Assignee of the ticket, if not mentioned keep it null") String assignee,
            @ToolParam(description = "Set to true ONLY if the user explicitly confirms they want to bypass duplicate checks.") Boolean forceCreate
    ) {
        if (forceCreate == null || !forceCreate) {
            String searchContent = title + " " + description;
            List<IncidentResponse> similarIncidents = incidentService.findSimilarIncidents(searchContent);

            if (!similarIncidents.isEmpty()) {
                StringBuilder warning = new StringBuilder();
                warning.append("WARNING: Found similar existing incidents in the database:\n");
                for (IncidentResponse inc : similarIncidents) {
                    warning.append("- [").append(inc.status()).append("] ").append(inc.title()).append("\n");
                }
                warning.append("\nDO NOT CREATE THIS TICKET YET. Ask the user if they still want to proceed. If they say yes, call this tool again with forceCreate=true.");

                return warning.toString();
            }
        }
        
        CreateIncidentRequest request = new CreateIncidentRequest(
                title, description, IncidentSeverity.valueOf(severity.toUpperCase()), assignee);

        return incidentService.createIncident(request);
    }


    @Tool(description = """
            Search the incidents table dynamically to find historical or active tickets/events.
            DO NOT use this tool if the user is asking 'how to fix' something; use searchRunbooks instead.    """)
    public List<IncidentResponse> searchIncidents(
            @ToolParam(required = false, description = "Only set if the user explicitly filters by status (OPEN, IN_PROGRESS, CLOSED). Omit otherwise.")
            IncidentStatus status,
            @ToolParam(required = false, description = "Only set if the user explicitly filters by severity (LOW, MEDIUM, HIGH). Omit otherwise.")
            IncidentSeverity severity,
            @ToolParam(required = false, description = "Only set if the user explicitly names an assignee. Omit otherwise — never pass an empty string.")
            String assignee,
            @ToolParam(required = false, description = "Only set for genuine content/symptom search, e.g. 'database connection issues'. Omit for plain listing requests.")
            String keyword,
            @ToolParam(required = false, description = "Entity field to sort by, using the exact camelCase Java field name from getSchema (e.g. 'createdAt'), not the DB column name. Omit to sort by most recent.")
            String sortProperty,
            @ToolParam(required = false, description = "ASC or DESC. Defaults to DESC (most recent first) if omitted.")
            String sortDirection,
            @ToolParam(required = false, description = "Max results to return. Defaults to 10 if omitted.")
            Integer limit
    ) {
        SearchIncidentRequest request = new SearchIncidentRequest(
                status, severity,
                StringUtils.isBlank(assignee) ? null : assignee,
                StringUtils.isBlank(keyword) ? null : keyword,
                sortProperty, sortDirection, limit);
        return incidentService.dynamicSearch(request);
    }

}
