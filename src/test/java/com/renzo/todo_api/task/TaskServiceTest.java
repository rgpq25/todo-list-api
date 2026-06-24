package com.renzo.todo_api.task;

import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import com.renzo.todo_api.task.services.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnEmptyListWhenNoTasksExist() {
        when(taskRepository.findAll()).thenReturn(List.of());

        List<TaskResponse> tasks = taskService.findAll();

        assertThat(tasks).isEmpty();
        verify(taskRepository).findAll();
        verifyNoInteractions(taskMapper);
    }

    @Test
    void shouldReturnAllTasks() {
        Task task1 = Task.builder()
                .id(1L)
                .title("First task")
                .description("First description")
                .completed(false)
                .priority(TaskPriority.LOW)
                .build();
        Task task2 = Task.builder()
                .id(2L)
                .title("Second task")
                .description("Second description")
                .completed(true)
                .priority(TaskPriority.MEDIUM)
                .build();

        TaskResponse response1 = new TaskResponse(
                task1.getId(),
                task1.getTitle(),
                task1.getDescription(),
                task1.getCompleted(),
                task1.getPriority(),
                task1.getDueDate(),
                task1.getCreatedAt(),
                task1.getUpdatedAt()
        );
        TaskResponse response2 = new TaskResponse(
                task2.getId(),
                task2.getTitle(),
                task2.getDescription(),
                task2.getCompleted(),
                task2.getPriority(),
                task2.getDueDate(),
                task2.getCreatedAt(),
                task2.getUpdatedAt()
        );

        List<Task> mockTasks = List.of(task1, task2);

        when(taskRepository.findAll()).thenReturn(mockTasks);
        when(taskMapper.toResponse(task1)).thenReturn(response1);
        when(taskMapper.toResponse(task2)).thenReturn(response2);

        List<TaskResponse> tasks = taskService.findAll();

        assertThat(tasks).containsExactly(response1, response2);
        verify(taskRepository).findAll();
        verify(taskMapper).toResponse(task1);
        verify(taskMapper).toResponse(task2);
    }
}
