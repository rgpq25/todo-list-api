package com.renzo.todo_api.common;

public record FieldErrorResponse(
        String field,
        String code,
        String message,
        Object rejectedValue
) {
}
