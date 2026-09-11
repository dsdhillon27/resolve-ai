package com.dsd.resolveai.repository;

import com.dsd.resolveai.dto.IncidentFilter;
import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.enums.FilterOperator;
import com.dsd.resolveai.exception.InvalidFilterException;
import com.dsd.resolveai.service.EntitySchemaService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IncidentFilterTranslator {

    private static final String ENTITY = "Incident";
    private final EntitySchemaService schemaService;

    private record Resolved(String field, FilterOperator op, Object value, String rawValue) {}

    public Specification<Incident> toSpecification(List<IncidentFilter> filters) {
        List<Resolved> resolved = (filters == null ? List.<IncidentFilter>of() : filters)
                .stream().map(this::resolve).toList();          // validates eagerly

        return (root, query, cb) -> resolved.isEmpty() ? null
                : cb.and(resolved.stream()
                .map(r -> toPredicate(r, root, cb))
                .toArray(Predicate[]::new));
    }

    private Resolved resolve(IncidentFilter f) {
        Map<String, Class<?>> fields = schemaService.fields(ENTITY);
        Class<?> type = fields.get(f.field());
        if (type == null) {
            throw new InvalidFilterException("Unknown field '" + f.field()
                    + "'. Valid fields: " + String.join(", ", fields.keySet()));
        }
        if (f.operator() == FilterOperator.CONTAINS && type != String.class) {
            throw new InvalidFilterException("CONTAINS only works on text fields; '"
                    + f.field() + "' is " + type.getSimpleName() + ". Use EQ instead.");
        }
        return new Resolved(f.field(), f.operator(), convert(f.value(), type, f.field()), f.value());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate toPredicate(Resolved r, Root<Incident> root, CriteriaBuilder cb) {
        Path path = root.get(r.field());
        return switch (r.op()) {
            case EQ       -> cb.equal(path, r.value());
            case NE       -> cb.notEqual(path, r.value());
            case CONTAINS -> cb.like(cb.lower(path), "%" + r.rawValue().toLowerCase() + "%");
            case GT       -> cb.greaterThan(path, (Comparable) r.value());
            case GTE      -> cb.greaterThanOrEqualTo(path, (Comparable) r.value());
            case LT       -> cb.lessThan(path, (Comparable) r.value());
            case LTE      -> cb.lessThanOrEqualTo(path, (Comparable) r.value());
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convert(String raw, Class<?> type, String field) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidFilterException("Filter on '" + field + "' is missing a value.");
        }
        String v = raw.trim();
        try {
            if (type == String.class)  return v;
            if (type.isEnum())         return Enum.valueOf((Class<Enum>) type, v.toUpperCase());
            if (type == UUID.class)    return UUID.fromString(v);
            if (type == Instant.class) return Instant.parse(v);
            if (type == Integer.class) return Integer.valueOf(v);
            if (type == Long.class)    return Long.valueOf(v);
            if (type == Boolean.class) return Boolean.valueOf(v);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new InvalidFilterException(explain(field, type, v));
        }
        throw new InvalidFilterException("Field '" + field + "' has unsupported type "
                + type.getSimpleName() + " and cannot be filtered.");
    }

    private String explain(String field, Class<?> type, String raw) {
        if (type.isEnum()) {
            return "Invalid value '" + raw + "' for '" + field + "'. Allowed: "
                    + Arrays.stream(type.getEnumConstants()).map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
        return "Invalid value '" + raw + "' for '" + field
                + "' (expected " + type.getSimpleName()
                + (type == Instant.class ? ", ISO-8601 e.g. 2026-01-01T00:00:00Z" : "") + ").";
    }
}
