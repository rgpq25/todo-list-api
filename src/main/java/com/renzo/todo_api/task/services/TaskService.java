package com.renzo.todo_api.task.services;

import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.repositories.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> findAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(taskMapper::toResponse).toList();
    }
}
