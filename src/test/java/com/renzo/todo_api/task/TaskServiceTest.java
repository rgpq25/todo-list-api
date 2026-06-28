package com.renzo.todo_api.task;

import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import com.renzo.todo_api.task.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, new TaskMapper());
    }

    @Nested
    class FindAllTests {
        @Test
        void shouldReturnEmptyListWhenNoTasksExist() {
            when(taskRepository.findAll()).thenReturn(List.of());

            List<TaskResponse> tasks = taskService.findAll();

            assertThat(tasks).isEmpty();
            verify(taskRepository).findAll();
        }

        @Test
        void shouldReturnAllTasks() {
            Task task1 = Task.builder()
                    .id(1L)
                    .title("First task")
                    .description("First description")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .createdAt(LocalDateTime.of(2026, 6, 27, 10, 0))
                    .build();
            Task task2 = Task.builder()
                    .id(2L)
                    .title("Second task")
                    .description("Second description")
                    .completed(true)
                    .priority(TaskPriority.MEDIUM)
                    .createdAt(LocalDateTime.of(2026, 6, 27, 11, 0))
                    .build();

            when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

            List<TaskResponse> tasks = taskService.findAll();

            assertThat(tasks).containsExactly(
                    new TaskResponse(
                            1L,
                            "First task",
                            "First description",
                            false,
                            TaskPriority.LOW,
                            null,
                            LocalDateTime.of(2026, 6, 27, 10, 0),
                            null
                    ),
                    new TaskResponse(
                            2L,
                            "Second task",
                            "Second description",
                            true,
                            TaskPriority.MEDIUM,
                            null,
                            LocalDateTime.of(2026, 6, 27, 11, 0),
                            null
                    )
            );
            verify(taskRepository).findAll();
        }
    }

    @Nested
    class CreateTaskTests {
        @Test
        void shouldCreateTask() {
            TaskRequest request = new TaskRequest(
                    "Buy milk",
                    "Before 6pm",
                    TaskPriority.MEDIUM,
                    LocalDate.of(2026, 6, 30)
            );

            Task savedTask = Task.builder()
                    .id(1L)
                    .title("Buy milk")
                    .description("Before 6pm")
                    .completed(false)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(LocalDate.of(2026, 6, 30))
                    .createdAt(LocalDateTime.of(2026, 6, 27, 12, 0))
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            TaskResponse result = taskService.createTask(request);

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(taskCaptor.capture());

            Task taskToSave = taskCaptor.getValue();
            assertThat(taskToSave.getTitle()).isEqualTo("Buy milk");
            assertThat(taskToSave.getDescription()).isEqualTo("Before 6pm");
            assertThat(taskToSave.getCompleted()).isFalse();
            assertThat(taskToSave.getPriority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(taskToSave.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 30));

            assertThat(result).isEqualTo(new TaskResponse(
                    1L,
                    "Buy milk",
                    "Before 6pm",
                    false,
                    TaskPriority.MEDIUM,
                    LocalDate.of(2026, 6, 30),
                    LocalDateTime.of(2026, 6, 27, 12, 0),
                    null
            ));
        }

        @Test
        void shouldCreateTaskWithNullOptionalFields() {
            TaskRequest request = new TaskRequest(
                    "Buy milk",
                    null,
                    null,
                    null
            );

            Task savedTask = Task.builder()
                    .id(2L)
                    .title("Buy milk")
                    .description(null)
                    .completed(false)
                    .priority(null)
                    .dueDate(null)
                    .createdAt(LocalDateTime.of(2026, 6, 27, 12, 30))
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            TaskResponse result = taskService.createTask(request);

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(taskCaptor.capture());

            Task taskToSave = taskCaptor.getValue();
            assertThat(taskToSave.getTitle()).isEqualTo("Buy milk");
            assertThat(taskToSave.getDescription()).isNull();
            assertThat(taskToSave.getCompleted()).isFalse();
            assertThat(taskToSave.getPriority()).isNull();
            assertThat(taskToSave.getDueDate()).isNull();

            assertThat(result).isEqualTo(new TaskResponse(
                    2L,
                    "Buy milk",
                    null,
                    false,
                    null,
                    null,
                    LocalDateTime.of(2026, 6, 27, 12, 30),
                    null
            ));
        }
    }
}
