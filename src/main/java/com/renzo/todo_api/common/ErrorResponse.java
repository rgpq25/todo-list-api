package com.renzo.todo_api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String title;
    private String detail;

    @Builder.Default
    private List<FieldErrorResponse> errors = List.of();

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
