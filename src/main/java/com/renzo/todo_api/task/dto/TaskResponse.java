package com.renzo.todo_api.task.dto;

import com.renzo.todo_api.task.models.Task;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Boolean completed,
        Task.Priority priority,
        LocalDateTime dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
