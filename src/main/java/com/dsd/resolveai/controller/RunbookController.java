package com.dsd.resolveai.controller;

import com.dsd.resolveai.dto.CreateRunbookRequest;
import com.dsd.resolveai.dto.RunbookResponse;
import com.dsd.resolveai.dto.UpdateIncidentRequest;
import com.dsd.resolveai.dto.UpdateRunbookRequest;
import com.dsd.resolveai.entity.Runbook;
import com.dsd.resolveai.mapper.RunbookMapper;
import com.dsd.resolveai.service.IngestionService;
import com.dsd.resolveai.service.RunbookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runbooks")
@RequiredArgsConstructor
public class RunbookController {

    private final RunbookService runbookService;
    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<RunbookResponse> createRunbook(@Valid @RequestBody CreateRunbookRequest request) {
        RunbookResponse response = runbookService.createRunbook(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RunbookResponse> getRunbook(@PathVariable UUID id) {
        RunbookResponse response = runbookService.getRunbook(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RunbookResponse>> getRunbooks() {
        List<RunbookResponse> responses = runbookService.getAllRunbooks();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RunbookResponse> updateRunbook(@RequestBody UpdateRunbookRequest request, @PathVariable UUID id) {
        RunbookResponse response = runbookService.updateRunbook(request, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/ingest")
    public ResponseEntity<Void> ingest(@PathVariable UUID id) {
        ingestionService.ingest(id);
        return ResponseEntity.ok().build();
    }

}
