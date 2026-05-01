package com.saas.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class InviteResponse {
    private UUID userId;
    private String email;
    private String displayName;
    private String role;
    private String invitationToken;
    private String message;
}