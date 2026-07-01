package com.renzo.todo_api.task.exceptions;

import com.renzo.todo_api.common.exceptions.NotFoundException;

public class TaskNotFound extends NotFoundException {
    public TaskNotFound(Long id) {
        super("Task", id);
    }
}
