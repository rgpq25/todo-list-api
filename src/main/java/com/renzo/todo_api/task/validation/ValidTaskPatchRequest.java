package com.renzo.todo_api.task.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskPatchRequestValidator.class)
public @interface ValidTaskPatchRequest {
    String message() default "Invalid task patch request.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
