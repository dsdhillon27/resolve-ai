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
@Table(name = "runbook")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Runbook {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull(message = "Title cannot be null")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Content cannot be null")
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull(message = "Created At cannot be null")
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private Instant createdAt;
}
