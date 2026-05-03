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
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
    public List<IncidentResponse> dynamicSearch(Incident probe, String keyword, String sortProperty, String sortDirection, Integer limit) {

        Specification<Incident> spec = Specification.where(null);

        if (probe != null) {
            Example<Incident> example = Example.of(probe, ExampleMatcher.matchingAll().withIgnoreCase());
            spec = (root, query, cb) -> QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            var filterExp = b.eq("type", "incident");

            if (probe != null && probe.getStatus() != null) {
                filterExp = b.and(filterExp, b.eq("status", probe.getStatus().name()));
            }
            if (probe != null && probe.getSeverity() != null) {
                filterExp = b.and(filterExp, b.eq("severity", probe.getSeverity().name()));
            }
            List<Document> semanticMatches = vectorStore.similaritySearch(
                    SearchRequest.builder().query(keyword).filterExpression(filterExp.build()).build()
            );
            List<UUID> matchingIds = semanticMatches.stream()
                    .map(doc -> UUID.fromString(doc.getMetadata().get("incidentId").toString()))
                    .toList();
            if (matchingIds.isEmpty()) return List.of();

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

    @Transactional(readOnly = true)
    public List<IncidentResponse> findSimilarIncidents(String keyword) {

        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }

        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        var filterExpression = filterExpressionBuilder.eq("type", "incident");

        SearchRequest searchRequest = SearchRequest.builder()
                .query(keyword)
                .filterExpression(filterExpression.build())
                .similarityThreshold(0.85)
                .topK(3)
                .build();

        List<Document> semanticMatches = vectorStore.similaritySearch(searchRequest).stream().toList();

        List<UUID> matchindIdList = semanticMatches.stream()
                .map(document -> UUID.fromString(document.getMetadata().get("incidentId").toString()))
                .toList();

        return incidentRepository.findAllById(matchindIdList).stream()
                .map(IncidentMapper::toResponse)
                .toList();
    }

    private void saveIncidentInVectorDB(Incident savedIncident) {

        StringBuilder textContent = new StringBuilder("Title: " + savedIncident.getTitle() +
                "\nDescription: " + savedIncident.getDescription());

        if (savedIncident.getResolutionNotes() != null) {
            textContent.append("\nResolution Notes: " + savedIncident.getResolutionNotes());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "incident");
        metadata.put("incidentId", savedIncident.getId().toString());

        if (savedIncident.getStatus() != null) {
            metadata.put("status", savedIncident.getStatus().name());
        }

        if (savedIncident.getSeverity() != null) {
            metadata.put("severity", savedIncident.getSeverity().name());
        }

        if (savedIncident.getAssignee() != null) {
            metadata.put("assignee", savedIncident.getAssignee());
        }

        Document doc = new Document(String.valueOf(textContent), metadata);
        vectorStore.add(List.of(doc));
    }

}
