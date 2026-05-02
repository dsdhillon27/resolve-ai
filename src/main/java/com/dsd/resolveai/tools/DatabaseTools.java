package com.dsd.resolveai.tools;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseTools {

    private final EntityManager entityManager;

    @Tool(description = """
        Get the exact database schema (column names and data types) for a specific table/entity. 
        You MUST call this tool before dynamically searching an entity to know what fields exist!
    """)
    public String getSchema(
            @ToolParam(description = "The name of the entity/table, e.g., 'Incident' or 'Runbook'") String entityName
    ) {

        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {

            if (entity.getName().equalsIgnoreCase(entityName)) {

                StringBuilder schema = new StringBuilder("Schema for " + entity.getName() + ":\n");

                entity.getAttributes().forEach(attr -> {
                    schema.append("- ")
                            .append(attr.getName())
                            .append(" (Type: ").append(attr.getJavaType().getSimpleName()).append(")\n");
                });

                return schema.toString();
            }
        }

        return "Entity '" + entityName + "' not found. Available entities are: " +
                entityManager.getMetamodel().getEntities().stream().map(EntityType::getName).toList();
    }
}
