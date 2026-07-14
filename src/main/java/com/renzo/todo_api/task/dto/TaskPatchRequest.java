package com.renzo.todo_api.task.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.validation.ValidTaskPatchRequest;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@ValidTaskPatchRequest
@Getter
public class TaskPatchRequest {
    @JsonIgnore
    private final Set<String> fields = new HashSet<>();

    @Size(max = 50, message = "The title must have at most 50 characters.")
    private String title;

    @Size(max = 1000, message = "The description must have at most 1000 characters.")
    private String description;

    private Boolean completed;
    private TaskPriority priority;
    private LocalDate dueDate;

    public void setTitle(String title) {
        fields.add("title");
        this.title = title;
    }

    public void setDescription(String description) {
        fields.add("description");
        this.description = description;
    }

    public void setCompleted(Boolean completed) {
        fields.add("completed");
        this.completed = completed;
    }

    public void setPriority(TaskPriority priority) {
        fields.add("priority");
        this.priority = priority;
    }

    public void setDueDate(LocalDate dueDate) {
        fields.add("dueDate");
        this.dueDate = dueDate;
    }

    @JsonIgnore
    public boolean hasFields() {
        return !fields.isEmpty();
    }

    @JsonIgnore
    public boolean hasTitle() {
        return fields.contains("title");
    }

    @JsonIgnore
    public boolean hasDescription() {
        return fields.contains("description");
    }

    @JsonIgnore
    public boolean hasCompleted() {
        return fields.contains("completed");
    }

    @JsonIgnore
    public boolean hasPriority() {
        return fields.contains("priority");
    }

    @JsonIgnore
    public boolean hasDueDate() {
        return fields.contains("dueDate");
    }

}
