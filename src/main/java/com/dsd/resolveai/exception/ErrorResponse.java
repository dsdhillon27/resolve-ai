package com.dsd.resolveai.exception;

import java.time.Instant;

public record ErrorResponse(
        String error,
        String message,
        int status,
        Instant timestamp,
        String path
) {}
