package com.renzo.todo_api.task.dto;

import com.renzo.todo_api.task.models.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotNull(message = "The title must be supplied.")
        @NotBlank(message = "The title must not be empty.")
        @Size(max = 50, message = "The title must have at most 50 characters.")
        String title,

        @Size(max = 1000, message = "The description must have at most 1000 characters.")
        String description,
        
        TaskPriority priority,
        LocalDate dueDate
) {
}
