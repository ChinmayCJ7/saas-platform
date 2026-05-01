package com.saas.platform.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {

    @Size(min = 2, max = 150)
    private String name;

    private String description;

    private String status;
}