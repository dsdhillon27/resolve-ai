package com.dsd.resolveai.service;

import com.dsd.resolveai.dto.CreateRunbookRequest;
import com.dsd.resolveai.dto.RunbookResponse;
import com.dsd.resolveai.dto.UpdateRunbookRequest;
import com.dsd.resolveai.entity.Runbook;
import com.dsd.resolveai.exception.ResourceNotFoundException;
import com.dsd.resolveai.mapper.RunbookMapper;
import com.dsd.resolveai.repository.RunbookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RunbookService {

    private final RunbookRepository runbookRepository;
    private final VectorStore vectorStore;

    @Transactional
    public RunbookResponse createRunbook(CreateRunbookRequest request) {
        Runbook runbook = RunbookMapper.toEntity(request);
        Runbook savedRunbook = runbookRepository.save(runbook);

        saveRunbookInVectorDB(savedRunbook);

        return RunbookMapper.toResponse(savedRunbook);
    }

    @Transactional(readOnly = true)
    public RunbookResponse getRunbook(UUID id) {
        Runbook runbook = runbookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Runbook not found"));
        return RunbookMapper.toResponse(runbook);
    }

    @Transactional(readOnly = true)
    public List<RunbookResponse> getAllRunbooks() {
        List<Runbook> runbooks = runbookRepository.findAll();
        List<RunbookResponse> responses = runbooks.stream()
                .map(RunbookMapper::toResponse)
                .toList();
        return responses;
    }

    @Transactional
    public RunbookResponse updateRunbook(UpdateRunbookRequest request, UUID id) {
        Runbook runbook = runbookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(
                        String.format("Runbook with id %s not found", id)));

        Runbook updatedRunbook = RunbookMapper.updateEntityFromDto(request, runbook);
        Runbook savedRunbook = runbookRepository.save(updatedRunbook);
        return RunbookMapper.toResponse(savedRunbook);
    }

    private void saveRunbookInVectorDB(Runbook savedRunbook) {
        Document document = new Document(
                "Title: " + savedRunbook.getTitle() + "\n\n" + savedRunbook.getContent(),
                Map.of(
                        "type", "runbook",
                        "runbookId", savedRunbook.getId().toString(),
                        "title", savedRunbook.getTitle()
                )
        );

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> chunks = splitter.apply(List.of(document));

        vectorStore.add(chunks);
    }

}
