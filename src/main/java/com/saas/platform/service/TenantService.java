package com.saas.platform.service;

import com.saas.platform.dto.request.InviteRequest;
import com.saas.platform.dto.request.UpdateRoleRequest;
import com.saas.platform.dto.response.InviteResponse;
import com.saas.platform.dto.response.TenantResponse;
import com.saas.platform.dto.response.UserResponse;
import com.saas.platform.entity.Tenant;
import com.saas.platform.entity.User;
import com.saas.platform.entity.enums.Role;
import com.saas.platform.exception.ResourceNotFoundException;
import com.saas.platform.repository.ProjectRepository;
import com.saas.platform.repository.TenantRepository;
import com.saas.platform.repository.UserRepository;
import com.saas.platform.security.TenantAwareUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    private TenantAwareUserDetails getCurrentUser() {
        return (TenantAwareUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ── GET TENANT DETAILS ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public TenantResponse getTenant() {
        UUID tenantId = getCurrentUser().getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        long projectCount = projectRepository.countByTenantId(tenantId);
        long memberCount = userRepository.findAllByTenantId(tenantId).size();

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .plan(tenant.getPlan().name())
                .planStatus(tenant.getPlanStatus().name())
                .projectCount(projectCount)
                .memberCount(memberCount)
                .build();
    }

    // ── LIST MEMBERS ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<UserResponse> getMembers() {
        UUID tenantId = getCurrentUser().getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        return userRepository.findAllByTenantId(tenantId)
                .stream()
                .map(u -> toUserResponse(u, tenant))
                .toList();
    }

    // ── INVITE MEMBER ─────────────────────────────────────────────────────
    @Transactional
    public InviteResponse inviteMember(InviteRequest request) {
        UUID tenantId = getCurrentUser().getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // check if email already exists in this tenant
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new IllegalArgumentException(
                    "User with email " + request.getEmail() + " already exists in this tenant");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        // create user with temp password — in production this would be an email link
        String tempPassword = UUID.randomUUID().toString();

        User user = User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        return InviteResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .invitationToken(tempPassword)
                .message("User invited. Share the invitationToken as their temporary password.")
                .build();
    }

    // ── UPDATE ROLE ───────────────────────────────────────────────────────
    @Transactional
    public UserResponse updateMemberRole(UUID userId, UpdateRoleRequest request) {
        UUID tenantId = getCurrentUser().getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ensure user belongs to same tenant
        if (!user.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("User not found in this tenant");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        user.setRole(role);
        return toUserResponse(userRepository.save(user), tenant);
    }

    // ── REMOVE MEMBER ─────────────────────────────────────────────────────
    @Transactional
    public void removeMember(UUID userId) {
        UUID tenantId = getCurrentUser().getTenantId();
        UUID currentUserId = getCurrentUser().getUserId();

        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot remove yourself from the tenant");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("User not found in this tenant");
        }

        userRepository.delete(user);
    }

    // ── MAPPER ────────────────────────────────────────────────────────────
    private UserResponse toUserResponse(User user, Tenant tenant) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .build();
    }
}