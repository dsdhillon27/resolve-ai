package com.dsd.resolveai.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntitySchemaService {

    private final EntityManager entityManager;

    /** Field name -> Java type. TreeMap keeps ordering stable across JVM restarts. */
    public Map<String, Class<?>> fields(String entityName) {
        EntityType<?> entity = entityManager.getMetamodel().getEntities().stream()
                .filter(e -> e.getName().equalsIgnoreCase(entityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown entity: " + entityName));

        Map<String, Class<?>> fields = new TreeMap<>();
        entity.getAttributes().forEach(attr -> fields.put(attr.getName(), attr.getJavaType()));
        return fields;
    }

    /** Human-readable schema for the system prompt. Enums list their allowed values. */
    public String describe(String entityName) {
        StringBuilder sb = new StringBuilder(entityName + " fields:\n");
        fields(entityName).forEach((name, type) -> {
            sb.append("- ").append(name).append(" (").append(type.getSimpleName());
            if (type.isEnum()) {
                sb.append(": ").append(Arrays.stream(type.getEnumConstants())
                        .map(Object::toString).collect(Collectors.joining("|")));
            }
            sb.append(")\n");
        });
        return sb.toString();
    }
}
