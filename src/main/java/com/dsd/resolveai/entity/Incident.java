package com.dsd.resolveai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Incident {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Title cannot be null")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Description cannot be null")
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false)
    private String status;

    @NotNull(message = "Severity cannot be null")
    @Column(name = "severity", nullable = false)
    private String severity;

    @NotNull(message = "Created At cannot be null")
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private Instant createdAt;
}
