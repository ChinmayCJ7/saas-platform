package com.saas.platform.service;

import com.saas.platform.dto.request.CreateTaskRequest;
import com.saas.platform.dto.request.UpdateTaskRequest;
import com.saas.platform.dto.response.TaskResponse;
import com.saas.platform.dto.response.UserResponse;
import com.saas.platform.entity.Project;
import com.saas.platform.entity.Task;
import com.saas.platform.entity.User;
import com.saas.platform.entity.enums.Priority;
import com.saas.platform.entity.enums.TaskStatus;
import com.saas.platform.exception.ResourceNotFoundException;
import com.saas.platform.repository.ProjectRepository;
import com.saas.platform.repository.TaskRepository;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private TenantAwareUserDetails getCurrentUser() {
        return (TenantAwareUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ── GET ALL ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(UUID projectId, String status, Pageable pageable) {
        UUID tenantId = getCurrentUser().getTenantId();

        // verify project belongs to tenant
        projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        if (status != null) {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
            return taskRepository
                    .findAllByProjectIdAndTenantIdAndStatus(projectId, tenantId, taskStatus, pageable)
                    .map(this::toResponse);
        }

        return taskRepository
                .findAllByProjectIdAndTenantId(projectId, tenantId, pageable)
                .map(this::toResponse);
    }

    // ── GET ONE ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID projectId, UUID taskId) {
        UUID tenantId = getCurrentUser().getTenantId();

        projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found: " + taskId));

        return toResponse(task);
    }

    // ── CREATE ────────────────────────────────────────────────────────────
    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
        TenantAwareUserDetails currentUser = getCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        User creator = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found: " + request.getAssigneeId()));
        }

        Priority priority = request.getPriority() != null
                ? Priority.valueOf(request.getPriority().toUpperCase())
                : Priority.MEDIUM;

        Task task = Task.builder()
                .tenant(project.getTenant())
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .priority(priority)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .createdBy(creator)
                .build();

        return toResponse(taskRepository.save(task));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────
    @Transactional
    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        UUID tenantId = getCurrentUser().getTenantId();

        projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found: " + taskId));

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null)
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        if (request.getPriority() != null)
            task.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        return toResponse(taskRepository.save(task));
    }

    // ── DELETE ────────────────────────────────────────────────────────────
    @Transactional
    public void deleteTask(UUID projectId, UUID taskId) {
        UUID tenantId = getCurrentUser().getTenantId();

        projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));

        Task task = taskRepository.findByIdAndTenantId(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found: " + taskId));

        taskRepository.delete(task);
    }

    // ── MAPPER ────────────────────────────────────────────────────────────
    private TaskResponse toResponse(Task task) {
        UserResponse assigneeResponse = task.getAssignee() != null
                ? UserResponse.builder()
                  .id(task.getAssignee().getId())
                  .email(task.getAssignee().getEmail())
                  .displayName(task.getAssignee().getDisplayName())
                  .role(task.getAssignee().getRole().name())
                  .tenantId(task.getTenant().getId())
                  .tenantName(task.getTenant().getName())
                  .build()
                : null;

        UserResponse createdByResponse = UserResponse.builder()
                .id(task.getCreatedBy().getId())
                .email(task.getCreatedBy().getEmail())
                .displayName(task.getCreatedBy().getDisplayName())
                .role(task.getCreatedBy().getRole().name())
                .tenantId(task.getTenant().getId())
                .tenantName(task.getTenant().getName())
                .build();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .tenantId(task.getTenant().getId())
                .assignee(assigneeResponse)
                .createdBy(createdByResponse)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}