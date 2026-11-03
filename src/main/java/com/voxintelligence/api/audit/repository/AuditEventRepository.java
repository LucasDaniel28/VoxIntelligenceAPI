package com.voxintelligence.api.audit.repository;

import com.voxintelligence.api.audit.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType, UUID entityId);
}
