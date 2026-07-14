package com.renzo.todo_api.task.controllers;

import com.renzo.todo_api.task.dto.TaskPatchRequest;
import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.dto.TaskUpdateRequest;
import com.renzo.todo_api.task.exceptions.TaskNotFound;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskRepository) {
        this.taskService = taskRepository;
    }

    @GetMapping("")
    public ResponseEntity<List<TaskResponse>> getTasksWithFilters(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) LocalDate dueBefore,
            @RequestParam(required = false) LocalDate dueAfter
    ) {
        List<TaskResponse> tasks = taskService.getAllWithFilters(completed, priority, dueBefore, dueAfter);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id).orElseThrow(() -> new TaskNotFound(id));
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PostMapping("")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest task) {
        TaskResponse createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest task) {
        TaskResponse updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> patchTask(@PathVariable Long id, @Valid @RequestBody TaskPatchRequest task) {
        TaskResponse patchedTask = taskService.patchTask(id, task);
        return ResponseEntity.status(HttpStatus.OK).body(patchedTask);
    }
}
