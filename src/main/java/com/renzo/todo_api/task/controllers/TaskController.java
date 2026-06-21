package com.renzo.todo_api.task.controllers;

import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.services.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskRepository) {
        this.taskService = taskRepository;
    }

    @GetMapping("")
    public List<TaskResponse> getAllTasks() {
        return taskService.findAll();
    }
}
