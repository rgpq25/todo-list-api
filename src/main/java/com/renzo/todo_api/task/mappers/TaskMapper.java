package com.renzo.todo_api.task.mappers;

import com.renzo.todo_api.task.dto.TaskResponse;
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
}