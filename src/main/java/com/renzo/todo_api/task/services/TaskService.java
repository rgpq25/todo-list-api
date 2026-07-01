package com.renzo.todo_api.task.services;

import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> getAllWithFilters(
            Boolean completed,
            TaskPriority priority,
            LocalDate dueBefore,
            LocalDate dueAfter
    ) {
        List<Task> tasks = taskRepository.findAllWithFilters(completed, priority, dueBefore, dueAfter);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    public Optional<TaskResponse> getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(taskMapper::toResponse);
    }

    public TaskResponse createTask(TaskRequest taskDto) {
        Task taskEntity = taskMapper.toEntity(taskDto);
        Task createdTask = taskRepository.save(taskEntity);
        return taskMapper.toResponse(createdTask);
    }
}
