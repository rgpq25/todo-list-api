package com.renzo.todo_api.task.services;

import com.renzo.todo_api.task.dto.TaskCreateRequest;
import com.renzo.todo_api.task.dto.TaskPatchRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.dto.TaskUpdateRequest;
import com.renzo.todo_api.task.exceptions.TaskNotFound;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    public TaskResponse createTask(TaskCreateRequest taskDto) {
        Task taskEntity = taskMapper.toEntity(taskDto);
        Task createdTask = taskRepository.save(taskEntity);
        return taskMapper.toResponse(createdTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFound(id));
        taskMapper.updateEntity(task, taskDto);
        task.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse patchTask(Long id, TaskPatchRequest taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFound(id));
        taskMapper.patchEntity(task, taskDto);
        task.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse completeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFound(id));
        task.setCompleted(true);
        task.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse incompleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFound(id));
        task.setCompleted(false);
        task.setUpdatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        return taskMapper.toResponse(task);
    }
}
