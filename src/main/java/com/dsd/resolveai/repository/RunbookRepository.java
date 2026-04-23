package com.dsd.resolveai.repository;

import com.dsd.resolveai.entity.Runbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RunbookRepository extends JpaRepository<Runbook, UUID> {
}
