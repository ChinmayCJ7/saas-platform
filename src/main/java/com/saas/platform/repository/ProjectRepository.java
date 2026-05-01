package com.saas.platform.repository;

import com.saas.platform.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findAllByTenantId(UUID tenantId, Pageable pageable);
    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);
    long countByTenantId(UUID tenantId);
}