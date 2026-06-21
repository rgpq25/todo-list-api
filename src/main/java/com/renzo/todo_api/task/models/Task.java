package com.renzo.todo_api.task.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = true, length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(nullable = true)
    private LocalDateTime dueDate;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(insertable = false, updatable = true)
    private LocalDateTime updatedAt = null;
}
