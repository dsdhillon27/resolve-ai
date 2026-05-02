package com.dsd.resolveai.tools;

import com.dsd.resolveai.service.RunbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RunbookTools {

    private final RunbookService runbookService;

    @Tool(description = """
            Search the runbooks and return the matching content.
            """)
    public List<String> searchRunbooks(
            @ToolParam(description = "Natural language keyword to search runbooks.")
            String keyword) {
        return runbookService.searchRunbook(keyword);
    }
}
