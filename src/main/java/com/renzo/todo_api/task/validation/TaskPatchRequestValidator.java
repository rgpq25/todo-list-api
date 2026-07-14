package com.renzo.todo_api.task.validation;

import com.renzo.todo_api.task.dto.TaskPatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskPatchRequestValidator implements ConstraintValidator<ValidTaskPatchRequest, TaskPatchRequest> {
    @Override
    public boolean isValid(TaskPatchRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!request.hasFields()) {
            addFieldViolation(context, "request", "At least one field must be supplied.");
            valid = false;
        }

        if (request.hasTitle() && request.getTitle() == null) {
            addFieldViolation(context, "title", "The title must be supplied.");
            valid = false;
        }

        if (request.hasTitle() && request.getTitle() != null && request.getTitle().isBlank()) {
            addFieldViolation(context, "title", "The title must not be empty.");
            valid = false;
        }

        if (request.hasCompleted() && request.getCompleted() == null) {
            addFieldViolation(context, "completed", "The completed status must be supplied.");
            valid = false;
        }

        return valid;
    }

    private void addFieldViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
