package com.dsd.resolveai.mapper;


import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.dto.UpdateIncidentRequest;
import com.dsd.resolveai.entity.Incident;
import org.apache.commons.lang3.StringUtils;

import static com.dsd.resolveai.enums.IncidentStatus.OPEN;

public class IncidentMapper {

    public static Incident toEntity(CreateIncidentRequest request) {
        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setStatus(OPEN);
        incident.setSeverity(request.severity());
        return incident;
    }

    public static IncidentResponse toResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getCreatedAt(),
                incident.getAssignee(),
                incident.getResolutionNotes()
        );
    }

    public static Incident updateEntityFromDto(UpdateIncidentRequest request, Incident incident) {
        if (StringUtils.isNotBlank(request.title())) {
            incident.setTitle(request.title());
        }
        if(StringUtils.isNotBlank(request.description())){
            incident.setDescription(request.description());
        }
        if (request.status() != null) {
            incident.setStatus(request.status());
        }
        if (request.severity() != null) {
            incident.setSeverity(request.severity());
        }

        return incident;
    }
}
