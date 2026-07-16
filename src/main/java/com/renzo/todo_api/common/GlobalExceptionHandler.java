package com.renzo.todo_api.common;

import com.renzo.todo_api.common.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);

        List<FieldErrorResponse> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getField(),
                        error.getCode(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Validation failed")
                .detail("One or more request fields are invalid.")
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Invalid request body")
                .detail("Request body is malformed or contains unreadable values.")
                .errors(List.of())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Invalid request parameter")
                .detail("One or more request parameters are invalid.")
                .errors(List.of(new FieldErrorResponse(
                        e.getName(),
                        "TypeMismatch",
                        "Invalid value for request parameter.",
                        e.getValue()
                )))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title(e.getResourceName() + " not found")
                .detail(e.getMessage())
                .errors(List.of())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleMissingEndpoint(Exception e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Endpoint not found")
                .detail("The requested endpoint does not exist.")
                .errors(List.of())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleExceptions(Exception e) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Internal server error")
                .detail("An unexpected error occurred.")
                .errors(List.of())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
