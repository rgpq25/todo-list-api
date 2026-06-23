package com.renzo.todo_api.task.controllers;

import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskRepository) {
        this.taskService = taskRepository;
    }

    @GetMapping("")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PostMapping("")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest task) {
        TaskResponse createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }
}
