package com.renzo.todo_api.task.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task {
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
    private TaskPriority priority;

    @Column(nullable = true)
    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(insertable = false)
    private LocalDateTime updatedAt = null;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
