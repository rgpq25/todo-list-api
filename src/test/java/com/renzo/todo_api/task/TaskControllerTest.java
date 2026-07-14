package com.renzo.todo_api.task;

import com.renzo.todo_api.task.controllers.TaskController;
import com.renzo.todo_api.task.dto.TaskPatchRequest;
import com.renzo.todo_api.task.dto.TaskRequest;
import com.renzo.todo_api.task.dto.TaskResponse;
import com.renzo.todo_api.task.dto.TaskUpdateRequest;
import com.renzo.todo_api.task.exceptions.TaskNotFound;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.services.TaskService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Nested
    class GetTasksWithFiltersTests {
        @Test
        void getTasksWithFilters_NoParams_ReturnsTasks() throws Exception {
            when(taskService.getAllWithFilters(null, null, null, null)).thenReturn(List.of(
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
                            TaskPriority.HIGH,
                            LocalDate.of(2026, 7, 1),
                            LocalDateTime.of(2026, 6, 27, 11, 0),
                            null
                    )
            ));

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("First task"))
                    .andExpect(jsonPath("$[0].completed").value(false))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].priority").value("HIGH"))
                    .andExpect(jsonPath("$[1].dueDate").value("2026-07-01"));

            verify(taskService).getAllWithFilters(null, null, null, null);
        }

        @Test
        void getTasksWithFilters_AllParams_ReturnsTasks() throws Exception {
            when(taskService.getAllWithFilters(
                    false,
                    TaskPriority.HIGH,
                    LocalDate.of(2026, 7, 2),
                    LocalDate.of(2026, 6, 1)
            )).thenReturn(List.of(
                    new TaskResponse(
                            1L,
                            "First task",
                            "First description",
                            false,
                            TaskPriority.HIGH,
                            LocalDate.of(2026, 6, 25),
                            LocalDateTime.of(2026, 6, 27, 10, 0),
                            null
                    ),
                    new TaskResponse(
                            2L,
                            "Second task",
                            "Second description",
                            false,
                            TaskPriority.HIGH,
                            LocalDate.of(2026, 7, 1),
                            LocalDateTime.of(2026, 6, 27, 11, 0),
                            null
                    )
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("completed", "false")
                            .param("priority", "HIGH")
                            .param("dueBefore", "2026-07-02")
                            .param("dueAfter", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("First task"))
                    .andExpect(jsonPath("$[0].completed").value(false))
                    .andExpect(jsonPath("$[0].priority").value("HIGH"))
                    .andExpect(jsonPath("$[0].dueDate").value("2026-06-25"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("Second task"))
                    .andExpect(jsonPath("$[1].completed").value(false))
                    .andExpect(jsonPath("$[1].priority").value("HIGH"))
                    .andExpect(jsonPath("$[1].dueDate").value("2026-07-01"));

            verify(taskService).getAllWithFilters(
                    false,
                    TaskPriority.HIGH,
                    LocalDate.of(2026, 7, 2),
                    LocalDate.of(2026, 6, 1)
            );
        }

        @Test
        void getTasksWithFilters_PriorityIsInvalid_Returns400() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .param("priority", "URGENT"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request parameter"))
                    .andExpect(jsonPath("$.detail").value("One or more request parameters are invalid."))
                    .andExpect(jsonPath("$.errors[0].field").value("priority"))
                    .andExpect(jsonPath("$.errors[0].code").value("TypeMismatch"));

            verifyNoInteractions(taskService);
        }

        @Test
        void getTasksWithFilters_DueBeforeIsInvalid_Returns400() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .param("dueBefore", "not-a-date"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request parameter"))
                    .andExpect(jsonPath("$.detail").value("One or more request parameters are invalid."))
                    .andExpect(jsonPath("$.errors[0].field").value("dueBefore"))
                    .andExpect(jsonPath("$.errors[0].code").value("TypeMismatch"));

            verifyNoInteractions(taskService);
        }

        @Test
        void getTasksWithFilters_CompletedIsInvalid_Returns400() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .param("completed", "not-a-boolean"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request parameter"))
                    .andExpect(jsonPath("$.detail").value("One or more request parameters are invalid."))
                    .andExpect(jsonPath("$.errors[0].field").value("completed"))
                    .andExpect(jsonPath("$.errors[0].code").value("TypeMismatch"));

            verifyNoInteractions(taskService);
        }
    }

    @Nested
    class GetTaskByIdTests {
        @Test
        void getTaskById_ExistingTaskId_ReturnsTaskResponse() throws Exception {
            TaskResponse taskResponse = new TaskResponse(
                    1L,
                    "First task",
                    "First description",
                    false,
                    TaskPriority.LOW,
                    null,
                    LocalDateTime.of(2026, 6, 27, 10, 0),
                    null
            );
            when(taskService.getTaskById(1L)).thenReturn(Optional.of(taskResponse));

            mockMvc.perform(get("/api/tasks/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("First task"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("LOW"))
                    .andExpect(jsonPath("$.dueDate").value(nullValue()));

            verify(taskService).getTaskById(1L);
        }

        @Test
        void getTaskById_NonExistingTaskId_Returns404() throws Exception {
            when(taskService.getTaskById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/tasks/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Task not found"))
                    .andExpect(jsonPath("$.detail").value("Task with id 999 was not found."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verify(taskService).getTaskById(999L);
        }
    }

    @Nested
    class CreateTaskTests {
        @Test
        void createTask_AllFields_ReturnsCreatedTask() throws Exception {
            when(taskService.createTask(any(TaskRequest.class))).thenReturn(new TaskResponse(
                    1L,
                    "Buy milk",
                    "Before 6pm",
                    false,
                    TaskPriority.MEDIUM,
                    LocalDate.of(2026, 6, 30),
                    LocalDateTime.of(2026, 6, 27, 12, 0),
                    null
            ));

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "Before 6pm",
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value("Before 6pm"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.dueDate").value("2026-06-30"))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));

            ArgumentCaptor<TaskRequest> requestCaptor = ArgumentCaptor.forClass(TaskRequest.class);
            verify(taskService).createTask(requestCaptor.capture());

            assertThat(requestCaptor.getValue()).isEqualTo(new TaskRequest(
                    "Buy milk",
                    "Before 6pm",
                    TaskPriority.MEDIUM,
                    LocalDate.of(2026, 6, 30)
            ));
        }

        @Test
        void createTask_TitleIsBlank_Returns400() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "   "
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotBlank")));

            verifyNoInteractions(taskService);
        }

        @Test
        void createTask_TitleIsNull_Returns400() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": null
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")));

            verifyNoInteractions(taskService);
        }

        @Test
        void createTask_TitleIsTooLong_Returns400() throws Exception {
            String title = "a".repeat(51);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "%s"
                                    }
                                    """.formatted(title)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            verifyNoInteractions(taskService);
        }

        @Test
        void createTask_PriorityIsInvalid_Returns400() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "priority": "URGENT"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request body"))
                    .andExpect(jsonPath("$.detail").value("Request body is malformed or contains unreadable values."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verifyNoInteractions(taskService);
        }
    }

    @Nested
    class UpdateTask {
        @Test
        void updateTask_ValidFields_Returns200() throws Exception {
            Long taskToUpdateId = 1L;
            TaskUpdateRequest taskToUpdateRequest = new TaskUpdateRequest(
                    "This is the title",
                    "A long description, but very much longer!",
                    true,
                    TaskPriority.HIGH,
                    LocalDate.of(2026, 6, 30)
            );

            TaskResponse taskResponse = new TaskResponse(
                    1L,
                    "This is the title",
                    "A long description, but very much longer!",
                    true,
                    TaskPriority.HIGH,
                    LocalDate.of(2026, 6, 30),
                    LocalDateTime.of(2026, 5, 23, 12, 0),
                    LocalDateTime.now()
            );
            when(taskService.updateTask(taskToUpdateId, taskToUpdateRequest)).thenReturn(taskResponse);

            mockMvc.perform(put("/api/tasks/{id}", taskToUpdateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "This is the title",
                                        "description": "A long description, but very much longer!",
                                        "completed": true,
                                        "priority": "HIGH",
                                        "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("This is the title"))
                    .andExpect(jsonPath("$.description").value("A long description, but very much longer!"))
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.dueDate").value("2026-06-30"))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(notNullValue()));

            verify(taskService).updateTask(taskToUpdateId, taskToUpdateRequest);
        }

        @Test
        void updateTask_NonExistingTaskId_Returns404() throws Exception {
            Long taskToUpdateId = 1L;
            TaskUpdateRequest taskToUpdateRequest = new TaskUpdateRequest(
                    "This is the title",
                    "A long description, but very much longer!",
                    true,
                    null,
                    null
            );
            when(taskService.updateTask(taskToUpdateId, taskToUpdateRequest)).thenThrow(new TaskNotFound(taskToUpdateId));

            mockMvc.perform(put("/api/tasks/{id}", taskToUpdateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "This is the title",
                                        "description": "A long description, but very much longer!",
                                        "completed": true
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Task not found"))
                    .andExpect(jsonPath("$.detail").value("Task with id 1 was not found."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verify(taskService).updateTask(taskToUpdateId, taskToUpdateRequest);
        }

        @Test
        void updateTask_TitleIsBlank_Returns400() throws Exception {
            Long taskToUpdateId = 1L;
            TaskUpdateRequest taskToUpdateRequest = new TaskUpdateRequest(
                    "  ",
                    "A long description, but very much longer!",
                    true,
                    null,
                    null
            );

            mockMvc.perform(put("/api/tasks/{id}", taskToUpdateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "  ",
                                      "description": "A long description, but very much longer!",
                                      "completed": true
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotBlank")));

            verifyNoInteractions(taskService);
        }

        @Test
        void updateTask_TitleIsNull_Returns400() throws Exception {
            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": null,
                                      "completed": true
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotNull")));

            verifyNoInteractions(taskService);
        }

        @Test
        void updateTask_TitleIsTooLong_Returns400() throws Exception {
            String title = "a".repeat(51);

            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "%s",
                                      "completed": true
                                    }
                                    """.formatted(title)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            verifyNoInteractions(taskService);
        }

        @Test
        void updateTask_CompletedIsNull_Returns400() throws Exception {
            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "completed": null
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("completed")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotNull")));

            verifyNoInteractions(taskService);
        }

        @Test
        void updateTask_DescriptionIsTooLong_Returns400() throws Exception {
            String description = "a".repeat(1001);

            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "%s",
                                      "completed": true
                                    }
                                    """.formatted(description)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("description")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            verifyNoInteractions(taskService);
        }

        @Test
        void updateTask_PriorityIsInvalid_Returns400() throws Exception {
            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "completed": true,
                                      "priority": "URGENT"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request body"))
                    .andExpect(jsonPath("$.detail").value("Request body is malformed or contains unreadable values."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verifyNoInteractions(taskService);
        }
    }

    @Nested
    class PatchTask {
        @Test
        void patchTask_SpecifiedField_Returns200AndPassesOnlySpecifiedField() throws Exception {
            Long taskId = 1L;
            when(taskService.patchTask(anyLong(), any(TaskPatchRequest.class))).thenReturn(new TaskResponse(
                    taskId,
                    "Read docs",
                    "Spring MVC testing",
                    false,
                    TaskPriority.LOW,
                    LocalDate.of(2026, 7, 10),
                    LocalDateTime.of(2026, 6, 27, 10, 0),
                    LocalDateTime.of(2026, 6, 27, 11, 0)
            ));

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "priority": "LOW"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.priority").value("LOW"));

            ArgumentCaptor<TaskPatchRequest> requestCaptor = ArgumentCaptor.forClass(TaskPatchRequest.class);
            verify(taskService).patchTask(eq(taskId), requestCaptor.capture());

            TaskPatchRequest request = requestCaptor.getValue();
            assertThat(request.hasPriority()).isTrue();
            assertThat(request.getPriority()).isEqualTo(TaskPriority.LOW);
            assertThat(request.hasTitle()).isFalse();
            assertThat(request.hasDescription()).isFalse();
            assertThat(request.hasCompleted()).isFalse();
            assertThat(request.hasDueDate()).isFalse();
        }

        @Test
        void patchTask_NullOptionalField_Returns200AndPassesSpecifiedNull() throws Exception {
            Long taskId = 1L;
            when(taskService.patchTask(anyLong(), any(TaskPatchRequest.class))).thenReturn(new TaskResponse(
                    taskId,
                    "Read docs",
                    null,
                    false,
                    TaskPriority.LOW,
                    LocalDate.of(2026, 7, 10),
                    LocalDateTime.of(2026, 6, 27, 10, 0),
                    LocalDateTime.of(2026, 6, 27, 11, 0)
            ));

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "description": null
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value(nullValue()));

            ArgumentCaptor<TaskPatchRequest> requestCaptor = ArgumentCaptor.forClass(TaskPatchRequest.class);
            verify(taskService).patchTask(eq(taskId), requestCaptor.capture());

            TaskPatchRequest request = requestCaptor.getValue();
            assertThat(request.hasDescription()).isTrue();
            assertThat(request.getDescription()).isNull();
        }

        @Test
        void patchTask_NonExistingTaskId_Returns404() throws Exception {
            Long taskId = 999L;
            when(taskService.patchTask(anyLong(), any(TaskPatchRequest.class))).thenThrow(new TaskNotFound(taskId));

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "completed": true
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Task not found"))
                    .andExpect(jsonPath("$.detail").value("Task with id 999 was not found."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verify(taskService).patchTask(anyLong(), any(TaskPatchRequest.class));
        }

        @Test
        void patchTask_EmptyBody_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("request")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_TitleIsNull_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": null
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_TitleIsBlank_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "  "
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_CompletedIsNull_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "completed": null
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("completed")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_TitleIsTooLong_Returns400() throws Exception {
            String title = "a".repeat(51);

            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "%s"
                                    }
                                    """.formatted(title)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_DescriptionIsTooLong_Returns400() throws Exception {
            String description = "a".repeat(1001);

            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "description": "%s"
                                    }
                                    """.formatted(description)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("description")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_PriorityIsInvalid_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "priority": "URGENT"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Invalid request body"))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verifyNoInteractions(taskService);
        }

        @Test
        void patchTask_DueDateIsInvalid_Returns400() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "dueDate": "not-a-date"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Invalid request body"))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            verifyNoInteractions(taskService);
        }
    }
}
