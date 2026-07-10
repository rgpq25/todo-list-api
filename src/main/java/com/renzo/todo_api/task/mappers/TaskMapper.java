package com.renzo.todo_api.task.mappers;

import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.dto.TaskUpdateRequest;
import com.renzo.todo_api.task.models.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task entity) {
        if (entity == null) return null;

        return new TaskResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCompleted(),
                entity.getPriority(),
                entity.getDueDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Task toEntity(TaskRequest response) {
        if (response == null) return null;

        return Task.builder()
                .title(response.title())
                .description(response.description())
                .completed(false)
                .priority(response.priority())
                .dueDate(response.dueDate())
                .build();
    }

    public void updateEntity(Task entity, TaskUpdateRequest request) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setCompleted(request.completed());
        entity.setPriority(request.priority());
        entity.setDueDate(request.dueDate());
    }
}