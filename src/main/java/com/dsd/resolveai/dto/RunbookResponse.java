package com.dsd.resolveai.dto;

import java.time.Instant;
import java.util.UUID;

public record RunbookResponse(
        UUID id,
        String title,
        String content,
        Instant createdAt
) { }
