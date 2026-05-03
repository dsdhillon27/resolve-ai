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
