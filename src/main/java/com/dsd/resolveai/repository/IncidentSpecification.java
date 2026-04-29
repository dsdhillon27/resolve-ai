package com.dsd.resolveai.repository;

import com.dsd.resolveai.entity.Incident;
import com.dsd.resolveai.enums.IncidentSeverity;
import com.dsd.resolveai.enums.IncidentStatus;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class IncidentSpecification {
    public static Specification<Incident> hasId(UUID id) {
        return (root, query, cb) -> id == null ? null : cb.equal(root.get("id"), id);
    }
    public static Specification<Incident> hasStatus(String status) {
        return (root, query, cb) -> (status == null || status.trim().isEmpty()) ? null :
                cb.equal(root.get("status"), IncidentStatus.valueOf(status.toUpperCase()));
    }
    public static Specification<Incident> hasSeverity(String severity) {
        return (root, query, cb) -> (severity == null || severity.trim().isEmpty()) ? null :
                cb.equal(root.get("severity"), IncidentSeverity.valueOf(severity.toUpperCase()));
    }
}
