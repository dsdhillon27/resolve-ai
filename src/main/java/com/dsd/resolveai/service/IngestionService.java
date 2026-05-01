package com.dsd.resolveai.service;

import com.dsd.resolveai.entity.Runbook;
import com.dsd.resolveai.repository.RunbookRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionService {

    private final RunbookRepository runbookRepository;
    private final VectorStore vectorStore;

    public IngestionService(RunbookRepository runbookRepository, VectorStore vectorStore) {
        this.runbookRepository = runbookRepository;
        this.vectorStore = vectorStore;
    }

    public void ingest(UUID id) {
        Runbook runbook = runbookRepository.findById(id).orElseThrow(
                () -> new RuntimeException(
                        String.format("Runbook with id %s not found", id)));

        Document document = new Document(
                runbook.getContent(),
                Map.of(
                        "type", "runbook",
                        "runbookId", runbook.getId().toString(),
                        "title", runbook.getTitle()
                )
        );

        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> chunks =
                splitter.apply(List.of(document));

        vectorStore.add(chunks);
    }
}
