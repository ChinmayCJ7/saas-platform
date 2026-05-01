package com.saas.platform.service;

import com.saas.platform.dto.request.CreateProjectRequest;
import com.saas.platform.dto.request.UpdateProjectRequest;
import com.saas.platform.dto.response.ProjectResponse;
import com.saas.platform.dto.response.UserResponse;
import com.saas.platform.entity.Project;
import com.saas.platform.entity.Tenant;
import com.saas.platform.entity.User;
import com.saas.platform.entity.enums.Plan;
import com.saas.platform.entity.enums.ProjectStatus;
import com.saas.platform.entity.enums.SubscriptionStatus;
import com.saas.platform.exception.PlanLimitExceededException;
import com.saas.platform.exception.ResourceNotFoundException;
import com.saas.platform.repository.ProjectRepository;
import com.saas.platform.repository.SubscriptionRepository;
import com.saas.platform.repository.TenantRepository;
import com.saas.platform.repository.UserRepository;
import com.saas.platform.security.TenantAwareUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    // ── helper: get current user from SecurityContext ─────────────────────
    private TenantAwareUserDetails getCurrentUser() {
        return (TenantAwareUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ── GET ALL ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        UUID tenantId = getCurrentUser().getTenantId();
        return projectRepository
                .findAllByTenantId(tenantId, pageable)
                .map(this::toResponse);
    }

    // ── GET ONE ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId) {
        UUID tenantId = getCurrentUser().getTenantId();
        Project project = projectRepository
                .findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));
        return toResponse(project);
    }

    // ── CREATE ────────────────────────────────────────────────────────────
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        TenantAwareUserDetails currentUser = getCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        // Plan limit check
        enforcePlanLimit(tenantId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .tenant(tenant)
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.ACTIVE)
                .createdBy(user)
                .build();

        return toResponse(projectRepository.save(project));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────
    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        UUID tenantId = getCurrentUser().getTenantId();

        Project project = projectRepository
                .findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(ProjectStatus.valueOf(request.getStatus().toUpperCase()));
        }

        return toResponse(projectRepository.save(project));
    }

    // ── DELETE ────────────────────────────────────────────────────────────
    @Transactional
    public void deleteProject(UUID projectId) {
        UUID tenantId = getCurrentUser().getTenantId();

        Project project = projectRepository
                .findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        projectRepository.delete(project);
    }

    // ── PLAN LIMIT CHECK ──────────────────────────────────────────────────
    private void enforcePlanLimit(UUID tenantId) {
        var subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        if (subscription.getPlan() == Plan.FREE) {
            long count = projectRepository.countByTenantId(tenantId);
            if (count >= 3) {
                throw new PlanLimitExceededException(
                        "Free plan allows a maximum of 3 projects. Upgrade to Pro to create more.");
            }
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────
    private ProjectResponse toResponse(Project project) {
        UserResponse createdBy = UserResponse.builder()
                .id(project.getCreatedBy().getId())
                .email(project.getCreatedBy().getEmail())
                .displayName(project.getCreatedBy().getDisplayName())
                .role(project.getCreatedBy().getRole().name())
                .tenantId(project.getTenant().getId())
                .tenantName(project.getTenant().getName())
                .build();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus().name())
                .tenantId(project.getTenant().getId())
                .createdBy(createdBy)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}