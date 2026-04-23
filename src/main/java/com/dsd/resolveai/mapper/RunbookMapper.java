package com.dsd.resolveai.mapper;

import com.dsd.resolveai.dto.CreateRunbookRequest;
import com.dsd.resolveai.dto.RunbookResponse;
import com.dsd.resolveai.dto.UpdateRunbookRequest;
import com.dsd.resolveai.entity.Runbook;
import org.apache.commons.lang3.StringUtils;

public class RunbookMapper {

    public static Runbook toEntity(CreateRunbookRequest request) {
        Runbook runbook = new Runbook();
        runbook.setTitle(request.title());
        runbook.setContent(request.content());
        return runbook;
    }

    public static RunbookResponse toResponse(Runbook runbook) {
        return new RunbookResponse(
                runbook.getId(),
                runbook.getTitle(),
                runbook.getContent(),
                runbook.getCreatedAt()
        );
    }

    public static Runbook updateEntityFromDto(UpdateRunbookRequest request, Runbook runbook) {
        if (StringUtils.isNotBlank(request.title())) {
            runbook.setTitle(request.title());
        }
        if (StringUtils.isNotBlank(request.content())) {
            runbook.setContent(request.content());
        }

        return runbook;
    }
}
