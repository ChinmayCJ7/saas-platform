package com.saas.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TenantResponse {
    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private String planStatus;
    private long projectCount;
    private long memberCount;
}