package com.dsd.resolveai.service;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.dto.UpdateIncidentRequest;
import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.exception.ResourceNotFoundException;
import com.dsd.resolveai.mapper.IncidentMapper;
import com.dsd.resolveai.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.dsd.resolveai.enums.IncidentStatus.OPEN;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        Incident incident = IncidentMapper.toEntity(request);
        Incident savedIncident = incidentRepository.save(incident);
        return IncidentMapper.toResponse(savedIncident);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID id) {
        Incident incident = incidentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Incident not found"));
        return IncidentMapper.toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {
        List<Incident> incidents = incidentRepository.findAll();
        List<IncidentResponse> responses = incidents.stream()
                .map(IncidentMapper::toResponse)
                .toList();
        return responses;
    }

    @Transactional
    public IncidentResponse updateIncident(UpdateIncidentRequest request, UUID id) {
        Incident incident = incidentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        String.format("Incident with id %s not found", id)));

        Incident updatedIncident = IncidentMapper.updateEntityFromDto(request, incident);
        Incident savedIncident = incidentRepository.save(updatedIncident);
        return IncidentMapper.toResponse(savedIncident);
    }
}
