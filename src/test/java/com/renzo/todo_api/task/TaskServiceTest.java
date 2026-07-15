package com.renzo.todo_api.task;

import com.renzo.todo_api.task.dto.TaskCreateRequest;
import com.renzo.todo_api.task.dto.TaskPatchRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.dto.TaskUpdateRequest;
import com.renzo.todo_api.task.exceptions.TaskNotFound;
import com.renzo.todo_api.task.mappers.TaskMapper;
import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import com.renzo.todo_api.task.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    class GetAllWithFiltersTests {
        @Test
        void getAllWithFilters_NoParams_ReturnsAllTasks() {
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

            when(taskRepository.findAllWithFilters(null, null, null, null)).thenReturn(List.of(task1, task2));

            List<TaskResponse> tasks = taskService.getAllWithFilters(null, null, null, null);

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
            verify(taskRepository).findAllWithFilters(null, null, null, null);
        }

        @Test
        void getAllWithFilters_AllParams_PassesFiltersAndReturnsTasks() {
            LocalDate dueBefore = LocalDate.of(2026, 7, 1);
            LocalDate dueAfter = LocalDate.of(2026, 6, 1);
            Task task = Task.builder()
                    .id(1L)
                    .title("Matching task")
                    .description("Matches every filter")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 6, 15))
                    .createdAt(LocalDateTime.of(2026, 6, 27, 10, 0))
                    .build();

            when(taskRepository.findAllWithFilters(false, TaskPriority.HIGH, dueBefore, dueAfter))
                    .thenReturn(List.of(task));

            List<TaskResponse> tasks = taskService.getAllWithFilters(false, TaskPriority.HIGH, dueBefore, dueAfter);

            assertThat(tasks).containsExactly(new TaskResponse(
                    1L,
                    "Matching task",
                    "Matches every filter",
                    false,
                    TaskPriority.HIGH,
                    LocalDate.of(2026, 6, 15),
                    LocalDateTime.of(2026, 6, 27, 10, 0),
                    null
            ));
            verify(taskRepository).findAllWithFilters(false, TaskPriority.HIGH, dueBefore, dueAfter);
        }
    }

    @Nested
    class GetTaskByIdTests {
        @Test
        void getTaskById_ExistingTaskId_ReturnsTaskResponse() {
            Task task = Task.builder()
                    .id(1L)
                    .title("First task")
                    .description("First description")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .createdAt(LocalDateTime.of(2026, 6, 27, 10, 0))
                    .build();
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            Optional<TaskResponse> taskResponse = taskService.getTaskById(1L);

            verify(taskRepository).findById(1L);

            assertThat(taskResponse).isPresent();
            assertThat(taskResponse).contains(new TaskResponse(
                    1L,
                    "First task",
                    "First description",
                    false,
                    TaskPriority.LOW,
                    null,
                    LocalDateTime.of(2026, 6, 27, 10, 0),
                    null
            ));
        }

        @Test
        void getTaskById_NonExistingTaskId_ReturnsEmptyOptional() {
            when(taskRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<TaskResponse> taskResponse = taskService.getTaskById(1L);

            verify(taskRepository).findById(1L);

            assertThat(taskResponse).isEmpty();
        }
    }

    @Nested
    class CreateTaskTests {
        @Test
        void createTask_AllFields_ReturnsCreatedTask() {
            TaskCreateRequest request = new TaskCreateRequest(
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
        void createTask_NullOptionalFields_ReturnsCreatedTask() {
            TaskCreateRequest request = new TaskCreateRequest(
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

    @Nested
    class UpdateTask {
        @Test
        void updateTask_AllFields_ReturnsUpdatedTask() {
            Task task = Task.builder()
                    .id(1L)
                    .title("titulo")
                    .description("descripcion")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(null)
                    .updatedAt(null)
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();

            TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(
                    "Nuevo titulo!",
                    "Otra descripcion, diferente!",
                    true,
                    TaskPriority.LOW,
                    LocalDate.of(2026, 6, 30)
            );

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse taskResponse = taskService.updateTask(1L, taskUpdateRequest);

            assertThat(taskResponse.id()).isEqualTo(1L);
            assertThat(taskResponse.title()).isEqualTo("Nuevo titulo!");
            assertThat(taskResponse.description()).isEqualTo("Otra descripcion, diferente!");
            assertThat(taskResponse.completed()).isTrue();
            assertThat(taskResponse.priority()).isEqualTo(TaskPriority.LOW);
            assertThat(taskResponse.dueDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(taskResponse.updatedAt()).isNotNull();
            assertThat(taskResponse.createdAt()).isEqualTo(task.getCreatedAt());

            verify(taskRepository).findById(1L);
        }

        @Test
        void updateTask_NullOptionalFields_ReturnsUpdatedTask() {
            Task task = Task.builder()
                    .id(1L)
                    .title("titulo")
                    .description("descripcion")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(null)
                    .updatedAt(null)
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();

            TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(
                    "Nuevo titulo!",
                    null,
                    true,
                    null,
                    null
            );

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse taskResponse = taskService.updateTask(1L, taskUpdateRequest);

            assertThat(taskResponse.id()).isEqualTo(1L);
            assertThat(taskResponse.title()).isEqualTo("Nuevo titulo!");
            assertThat(taskResponse.description()).isNull();
            assertThat(taskResponse.completed()).isTrue();
            assertThat(taskResponse.priority()).isNull();
            assertThat(taskResponse.dueDate()).isNull();
            assertThat(taskResponse.updatedAt()).isNotNull();
            assertThat(taskResponse.createdAt()).isEqualTo(task.getCreatedAt());

            verify(taskRepository).findById(1L);
        }

        @Test
        void updateTask_NonExistingTaskId_ThrowsTaskNotFoundException() {
            TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest(
                    "Nuevo titulo!",
                    null,
                    false,
                    null,
                    null
            );

            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(999L, taskUpdateRequest))
                    .isInstanceOf(TaskNotFound.class)
                    .hasMessage("Task with id 999 was not found.");

            verify(taskRepository).findById(999L);
            verifyNoMoreInteractions(taskRepository);
        }
    }

    @Nested
    class PatchTask {
        @Test
        void patchTask_OneSpecifiedField_PreservesOmittedFields() {
            Task task = Task.builder()
                    .id(1L)
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();
            TaskPatchRequest request = new TaskPatchRequest();
            request.setPriority(TaskPriority.LOW);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.patchTask(1L, request);

            assertThat(response.title()).isEqualTo("Read docs");
            assertThat(response.description()).isEqualTo("Spring MVC testing");
            assertThat(response.completed()).isFalse();
            assertThat(response.priority()).isEqualTo(TaskPriority.LOW);
            assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 3, 12, 0));
            assertThat(response.updatedAt()).isNotNull();
            verify(taskRepository).findById(1L);
        }

        @Test
        void patchTask_MultipleSpecifiedFields_PreservesOmittedFields() {
            Task task = Task.builder()
                    .id(1L)
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();
            TaskPatchRequest request = new TaskPatchRequest();
            request.setTitle("Write tests");
            request.setCompleted(true);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.patchTask(1L, request);

            assertThat(response.title()).isEqualTo("Write tests");
            assertThat(response.completed()).isTrue();
            assertThat(response.description()).isEqualTo("Spring MVC testing");
            assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
            assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 3, 12, 0));
            assertThat(response.updatedAt()).isNotNull();
            verify(taskRepository).findById(1L);
        }

        @Test
        void patchTask_NullOptionalFields_ClearsOnlySpecifiedOptionalFields() {
            Task task = Task.builder()
                    .id(1L)
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();
            TaskPatchRequest request = new TaskPatchRequest();
            request.setDescription(null);
            request.setPriority(null);
            request.setDueDate(null);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.patchTask(1L, request);

            assertThat(response.title()).isEqualTo("Read docs");
            assertThat(response.completed()).isFalse();
            assertThat(response.description()).isNull();
            assertThat(response.priority()).isNull();
            assertThat(response.dueDate()).isNull();
            assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 3, 12, 0));
            assertThat(response.updatedAt()).isNotNull();
            verify(taskRepository).findById(1L);
        }

        @Test
        void patchTask_NonExistingTaskId_ThrowsTaskNotFoundException() {
            TaskPatchRequest request = new TaskPatchRequest();
            request.setCompleted(true);
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.patchTask(999L, request))
                    .isInstanceOf(TaskNotFound.class)
                    .hasMessage("Task with id 999 was not found.");

            verify(taskRepository).findById(999L);
            verifyNoMoreInteractions(taskRepository);
        }
    }

    @Nested
    class CompleteTask {
        @Test
        void completeTask_ExistingTaskId_ReturnsCompletedTask() {
            Long taskId = 1L;
            Task task = Task.builder()
                    .id(taskId)
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            TaskResponse taskResponse = taskService.completeTask(taskId);

            assertThat(taskResponse.title()).isEqualTo("Read docs");
            assertThat(taskResponse.description()).isEqualTo("Spring MVC testing");
            assertThat(taskResponse.completed()).isTrue();
            assertThat(taskResponse.priority()).isEqualTo(TaskPriority.HIGH);
            assertThat(taskResponse.dueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(taskResponse.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 3, 12, 0));
            assertThat(taskResponse.updatedAt()).isNotNull();
            assertThat(task.getCompleted()).isTrue();
            assertThat(task.getUpdatedAt()).isEqualTo(taskResponse.updatedAt());

            verify(taskRepository).findById(taskId);
        }

        @Test
        void completeTask_NonExistingTaskId_ThrowsTaskNotFoundException() {
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.completeTask(taskId))
                    .isInstanceOf(TaskNotFound.class)
                    .hasMessage("Task with id 1 was not found.");

            verify(taskRepository).findById(taskId);
        }
    }

    @Nested
    class IncompleteTask {
        @Test
        void incompleteTask_ExistingTaskId_ReturnsIncompleteTask() {
            Long taskId = 1L;
            Task task = Task.builder()
                    .id(taskId)
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(true)
                    .priority(TaskPriority.HIGH)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0))
                    .build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            TaskResponse taskResponse = taskService.incompleteTask(taskId);

            assertThat(taskResponse.title()).isEqualTo("Read docs");
            assertThat(taskResponse.description()).isEqualTo("Spring MVC testing");
            assertThat(taskResponse.completed()).isFalse();
            assertThat(taskResponse.priority()).isEqualTo(TaskPriority.HIGH);
            assertThat(taskResponse.dueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(taskResponse.createdAt()).isEqualTo(LocalDateTime.of(2026, 5, 3, 12, 0));
            assertThat(taskResponse.updatedAt()).isNotNull();
            assertThat(task.getCompleted()).isFalse();
            assertThat(task.getUpdatedAt()).isEqualTo(taskResponse.updatedAt());

            verify(taskRepository).findById(taskId);
        }

        @Test
        void incompleteTask_NonExistingTaskId_ThrowsTaskNotFoundException() {
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.incompleteTask(taskId))
                    .isInstanceOf(TaskNotFound.class)
                    .hasMessage("Task with id 1 was not found.");

            verify(taskRepository).findById(taskId);
        }
    }
}
