package com.saas.platform.repository;

import com.saas.platform.entity.Task;
import com.saas.platform.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findAllByProjectIdAndTenantId(UUID projectId, UUID tenantId, Pageable pageable);
    Page<Task> findAllByProjectIdAndTenantIdAndStatus(UUID projectId, UUID tenantId, TaskStatus status, Pageable pageable);
    Optional<Task> findByIdAndTenantId(UUID id, UUID tenantId);
}