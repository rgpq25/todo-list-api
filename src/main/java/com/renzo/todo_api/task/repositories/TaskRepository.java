package com.renzo.todo_api.task.repositories;

import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("""
            SELECT T FROM Task T
            WHERE (:completed is null or T.completed = :completed)
            AND (:priority is null or T.priority = :priority)
            AND (:dueBefore is null or T.dueDate < :dueBefore)
            AND (:dueAfter is null or T.dueDate > :dueAfter)
            """)
    List<Task> findAllWithFilters(
            @Param("completed") Boolean completed,
            @Param("priority") TaskPriority priority,
            @Param("dueBefore") LocalDate dueBefore,
            @Param("dueAfter") LocalDate dueAfter
    );
}
