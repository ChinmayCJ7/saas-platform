package com.saas.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200)
    private String title;

    private String description;
    private String priority;
    private LocalDate dueDate;
    private UUID assigneeId;
}