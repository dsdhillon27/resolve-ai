package com.dsd.resolveai.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRunbookRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Content is required") String content
) { }
