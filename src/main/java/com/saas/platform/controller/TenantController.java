package com.saas.platform.controller;

import com.saas.platform.dto.request.InviteRequest;
import com.saas.platform.dto.request.UpdateRoleRequest;
import com.saas.platform.dto.response.InviteResponse;
import com.saas.platform.dto.response.TenantResponse;
import com.saas.platform.dto.response.UserResponse;
import com.saas.platform.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/me")
    public ResponseEntity<TenantResponse> getTenant() {
        return ResponseEntity.ok(tenantService.getTenant());
    }

    @GetMapping("/me/members")
    public ResponseEntity<List<UserResponse>> getMembers() {
        return ResponseEntity.ok(tenantService.getMembers());
    }

    @PostMapping("/me/invite")
    public ResponseEntity<InviteResponse> inviteMember(
            @Valid @RequestBody InviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.inviteMember(request));
    }

    @PatchMapping("/me/members/{userId}/role")
    public ResponseEntity<UserResponse> updateMemberRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(tenantService.updateMemberRole(userId, request));
    }

    @DeleteMapping("/me/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID userId) {
        tenantService.removeMember(userId);
        return ResponseEntity.noContent().build();
    }
}