package com.dsd.resolveai.controller;

import com.dsd.resolveai.dto.CreateIncidentRequest;
import com.dsd.resolveai.dto.IncidentResponse;
import com.dsd.resolveai.dto.UpdateIncidentRequest;
import com.dsd.resolveai.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        IncidentResponse response = incidentService.createIncident(request);

        URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id())
                        .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable UUID id) {
        IncidentResponse response = incidentService.getIncident(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        List<IncidentResponse> responses = incidentService.getAllIncidents();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentResponse> updateIncident(@RequestBody UpdateIncidentRequest request, @PathVariable UUID id) {
        IncidentResponse response = incidentService.updateIncident(request, id);
        return ResponseEntity.ok(response);
    }
}
