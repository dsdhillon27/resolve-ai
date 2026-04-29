package com.dsd.resolveai.service;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.dto.UpdateIncidentRequest;
import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.exception.ResourceNotFoundException;
import com.dsd.resolveai.mapper.IncidentMapper;
import com.dsd.resolveai.repository.IncidentRepository;
import com.dsd.resolveai.repository.IncidentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final VectorStore vectorStore;

    @Transactional
    public IncidentResponse createIncident(CreateIncidentRequest request) {
        Incident incident = IncidentMapper.toEntity(request);
        Incident savedIncident = incidentRepository.save(incident);

        saveIncidentInVectorDB(savedIncident);

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

    @Transactional(readOnly = true)
    public List<IncidentResponse> searchIncidents(UUID id, String status, String severity, String keyword, String sortProperty, String sortDirection, Integer limit) {

        Specification<Incident> spec = Specification.allOf(
                IncidentSpecification.hasId(id),
                IncidentSpecification.hasStatus(status),
                IncidentSpecification.hasSeverity(severity)
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            var filterExp = b.eq("type", "incident");
            if (status != null && !status.trim().isEmpty()) {
                filterExp = b.and(filterExp, b.eq("status", status.toUpperCase()));
            }
            if (severity != null && !severity.trim().isEmpty()) {
                filterExp = b.and(filterExp, b.eq("severity", severity.toUpperCase()));
            }
            List<Document> semanticMatches = vectorStore.similaritySearch(
                    SearchRequest.builder().query(keyword).filterExpression(filterExp.build()).build()
            );
            List<UUID> matchingIds = semanticMatches.stream()
                    .map(doc -> UUID.fromString(doc.getMetadata().get("incidentId").toString()))
                    .toList();
            if (matchingIds.isEmpty()) return List.of(); // Semantic search found nothing

            spec = spec.and((root, query, cb) -> root.get("id").in(matchingIds));
        }

        Sort sort = Sort.unsorted();
        if (sortProperty != null && !sortProperty.trim().isEmpty()) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortProperty);
        }

        int actualLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 10;

        return incidentRepository.findAll(spec, PageRequest.of(0, actualLimit, sort))
                .stream()
                .map(IncidentMapper::toResponse)
                .toList();
    }

    private void saveIncidentInVectorDB(Incident savedIncident) {
        Document doc = new Document(
                "Title: " + savedIncident.getTitle() + "\nDescription: " + savedIncident.getDescription(),
                Map.of(
                        "type", "incident",
                        "incidentId", savedIncident.getId().toString(),
                        "status", savedIncident.getStatus().name(),
                        "severity", savedIncident.getSeverity().name()
                )
        );

        vectorStore.add(List.of(doc));
    }
}
