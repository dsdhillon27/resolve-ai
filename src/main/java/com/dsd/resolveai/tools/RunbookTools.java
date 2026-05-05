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
            Use this tool to find step-by-step instructions, guides, 
            or how-tos for fixing specific technical problems or errors 
            Runbooks contain solutions, not historical event logs.
            """)
    public List<String> searchRunbooks(
            @ToolParam(description = "Natural language keyword to search runbooks.")
            String keyword) {
        return runbookService.searchRunbook(keyword);
    }
}
