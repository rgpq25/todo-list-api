package com.renzo.todo_api.task;

import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }

    @Nested
    class GetTasksWithFiltersTests {
        @Test
        void getTasksWithFilters_NoParams_ReturnsAllTasks() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("Matching task")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Wrong completed")
                            .completed(true)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title").value("Matching task"))
                    .andExpect(jsonPath("$[0].completed").value(false))
                    .andExpect(jsonPath("$[0].priority").value("HIGH"))
                    .andExpect(jsonPath("$[0].dueDate").value("2026-06-15"))
                    .andExpect(jsonPath("$[1].title").value("Wrong completed"))
                    .andExpect(jsonPath("$[1].completed").value(true))
                    .andExpect(jsonPath("$[1].priority").value("HIGH"))
                    .andExpect(jsonPath("$[1].dueDate").value("2026-06-15"));
        }

        @Test
        void getTasksWithFilters_AllParams_ReturnsFilteredTasks() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("Matching task")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Wrong completed")
                            .completed(true)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Wrong priority")
                            .completed(false)
                            .priority(TaskPriority.LOW)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Due after boundary")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 1))
                            .build(),
                    Task.builder()
                            .title("Due before boundary")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 7, 1))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("completed", "false")
                            .param("priority", "HIGH")
                            .param("dueBefore", "2026-07-01")
                            .param("dueAfter", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Matching task"))
                    .andExpect(jsonPath("$[0].completed").value(false))
                    .andExpect(jsonPath("$[0].priority").value("HIGH"))
                    .andExpect(jsonPath("$[0].dueDate").value("2026-06-15"));
        }

        @Test
        void getTasksWithFilters_CompletedOnly_ReturnsMatchingTasks() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("Open task")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Done task")
                            .completed(true)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Another open task")
                            .completed(false)
                            .priority(TaskPriority.LOW)
                            .dueDate(LocalDate.of(2026, 7, 1))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("completed", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].title", containsInAnyOrder("Open task", "Another open task")));
        }

        @Test
        void getTasksWithFilters_PriorityOnly_ReturnsMatchingTasks() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("High priority task")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Low priority task")
                            .completed(false)
                            .priority(TaskPriority.LOW)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("Another high priority task")
                            .completed(true)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 7, 1))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("priority", "HIGH"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].title", containsInAnyOrder("High priority task", "Another high priority task")));
        }

        @Test
        void getTasksWithFilters_DueBeforeOnly_ExcludesBoundaryDate() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("Before date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 14))
                            .build(),
                    Task.builder()
                            .title("Boundary date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("After date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 16))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("dueBefore", "2026-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Before date"));
        }

        @Test
        void getTasksWithFilters_DueAfterOnly_ExcludesBoundaryDate() throws Exception {
            taskRepository.saveAll(List.of(
                    Task.builder()
                            .title("Before date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 14))
                            .build(),
                    Task.builder()
                            .title("Boundary date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 15))
                            .build(),
                    Task.builder()
                            .title("After date")
                            .completed(false)
                            .priority(TaskPriority.HIGH)
                            .dueDate(LocalDate.of(2026, 6, 16))
                            .build()
            ));

            mockMvc.perform(get("/api/tasks")
                            .param("dueAfter", "2026-06-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("After date"));
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

            assertThat(taskRepository.count()).isZero();
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

            assertThat(taskRepository.count()).isZero();
        }
    }

    @Nested
    class GetTaskByIdTests {
        @Test
        void getTaskById_ExistingTaskId_ReturnsTask() throws Exception {
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());

            mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(savedTask.getId()))
                    .andExpect(jsonPath("$.title").value("Read docs"))
                    .andExpect(jsonPath("$.description").value("Spring MVC testing"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("LOW"))
                    .andExpect(jsonPath("$.dueDate").value("2026-07-10"))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));
        }

        @Test
        void getTaskById_NonExistingTaskId_Returns404() throws Exception {
            Long missingId = 999999L;

            mockMvc.perform(get("/api/tasks/{id}", missingId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Task not found"))
                    .andExpect(jsonPath("$.detail").value("Task with id 999999 was not found."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));
        }
    }

    @Nested
    class CreateTaskTests {
        @Test
        void createTask_AllFields_ReturnsCreatedTask() throws Exception {
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
                    .andExpect(jsonPath("$.id").value(notNullValue()))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value("Before 6pm"))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.dueDate").value("2026-06-30"))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));

            List<Task> savedTasks = taskRepository.findAll();

            assertThat(savedTasks).hasSize(1);
            assertThat(savedTasks.getFirst().getTitle()).isEqualTo("Buy milk");
            assertThat(savedTasks.getFirst().getDescription()).isEqualTo("Before 6pm");
            assertThat(savedTasks.getFirst().getCompleted()).isFalse();
            assertThat(savedTasks.getFirst().getPriority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(savedTasks.getFirst().getDueDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(savedTasks.getFirst().getCreatedAt()).isNotNull();
            assertThat(savedTasks.getFirst().getUpdatedAt()).isNull();
        }

        @Test
        void createTask_TitleIsAtMaxLength_ReturnsCreatedTask() throws Exception {
            String title = "a".repeat(50);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "%s"
                                    }
                                    """.formatted(title)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(notNullValue()))
                    .andExpect(jsonPath("$.title").value(title))
                    .andExpect(jsonPath("$.description").value(nullValue()))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value(nullValue()))
                    .andExpect(jsonPath("$.dueDate").value(nullValue()))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));

            List<Task> savedTasks = taskRepository.findAll();

            assertThat(savedTasks).hasSize(1);
            assertThat(savedTasks.getFirst().getTitle()).isEqualTo(title);
            assertThat(savedTasks.getFirst().getDescription()).isNull();
            assertThat(savedTasks.getFirst().getCompleted()).isFalse();
        }

        @Test
        void createTask_DescriptionIsAtMaxLength_ReturnsCreatedTask() throws Exception {
            String description = "a".repeat(1000);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "%s"
                                    }
                                    """.formatted(description)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(notNullValue()))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value(description))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value(nullValue()))
                    .andExpect(jsonPath("$.dueDate").value(nullValue()))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));

            List<Task> savedTasks = taskRepository.findAll();

            assertThat(savedTasks).hasSize(1);
            assertThat(savedTasks.getFirst().getTitle()).isEqualTo("Buy milk");
            assertThat(savedTasks.getFirst().getDescription()).isEqualTo(description);
            assertThat(savedTasks.getFirst().getCompleted()).isFalse();
        }

        @Test
        void createTask_OptionalFieldsAreMissing_ReturnsCreatedTask() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(notNullValue()))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value(nullValue()))
                    .andExpect(jsonPath("$.completed").value(false))
                    .andExpect(jsonPath("$.priority").value(nullValue()))
                    .andExpect(jsonPath("$.dueDate").value(nullValue()))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(nullValue()));

            List<Task> savedTasks = taskRepository.findAll();

            assertThat(savedTasks).hasSize(1);
            assertThat(savedTasks.getFirst().getTitle()).isEqualTo("Buy milk");
            assertThat(savedTasks.getFirst().getDescription()).isNull();
            assertThat(savedTasks.getFirst().getCompleted()).isFalse();
            assertThat(savedTasks.getFirst().getPriority()).isNull();
            assertThat(savedTasks.getFirst().getDueDate()).isNull();
            assertThat(savedTasks.getFirst().getCreatedAt()).isNotNull();
            assertThat(savedTasks.getFirst().getUpdatedAt()).isNull();
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
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotNull")));

            assertThat(taskRepository.count()).isZero();
        }

        @Test
        void createTask_TitleIsBlank_Returns400() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "   ",
                                      "description": "Before 6pm",
                                      "priority": "LOW"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].code").value("NotBlank"));

            assertThat(taskRepository.count()).isZero();
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
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("title"))
                    .andExpect(jsonPath("$.errors[0].code").value("Size"));

            assertThat(taskRepository.count()).isZero();
        }

        @Test
        void createTask_DescriptionIsTooLong_Returns400() throws Exception {
            String description = "a".repeat(1001);

            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "%s"
                                    }
                                    """.formatted(description)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].field").value("description"))
                    .andExpect(jsonPath("$.errors[0].code").value("Size"));

            assertThat(taskRepository.count()).isZero();
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

            assertThat(taskRepository.count()).isZero();
        }

        @Test
        void createTask_BodyIsMalformed_Returns400() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Invalid request body"))
                    .andExpect(jsonPath("$.detail").value("Request body is malformed or contains unreadable values."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            assertThat(taskRepository.count()).isZero();
        }
    }

    @Nested
    class UpdateTask {
        @Test
        void updateTask_AllFields_ReturnsUpdatedTaskAndPersistsChanges() throws Exception {
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());
            LocalDateTime originalCreatedAt = taskRepository.findById(savedTask.getId()).orElseThrow().getCreatedAt();

            mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "Before 6pm",
                                      "completed": true,
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(savedTask.getId()))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value("Before 6pm"))
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.priority").value("MEDIUM"))
                    .andExpect(jsonPath("$.dueDate").value("2026-06-30"))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(notNullValue()));

            Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

            assertThat(updatedTask.getTitle()).isEqualTo("Buy milk");
            assertThat(updatedTask.getDescription()).isEqualTo("Before 6pm");
            assertThat(updatedTask.getCompleted()).isTrue();
            assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
            assertThat(updatedTask.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(updatedTask.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedTask.getUpdatedAt()).isNotNull();
        }

        @Test
        void updateTask_NullOptionalFields_ClearsOptionalFieldsAndPersistsChanges() throws Exception {
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());
            LocalDateTime originalCreatedAt = taskRepository.findById(savedTask.getId()).orElseThrow().getCreatedAt();

            mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": null,
                                      "completed": true,
                                      "priority": null,
                                      "dueDate": null
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(savedTask.getId()))
                    .andExpect(jsonPath("$.title").value("Buy milk"))
                    .andExpect(jsonPath("$.description").value(nullValue()))
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.priority").value(nullValue()))
                    .andExpect(jsonPath("$.dueDate").value(nullValue()))
                    .andExpect(jsonPath("$.createdAt").value(notNullValue()))
                    .andExpect(jsonPath("$.updatedAt").value(notNullValue()));

            Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

            assertThat(updatedTask.getTitle()).isEqualTo("Buy milk");
            assertThat(updatedTask.getDescription()).isNull();
            assertThat(updatedTask.getCompleted()).isTrue();
            assertThat(updatedTask.getPriority()).isNull();
            assertThat(updatedTask.getDueDate()).isNull();
            assertThat(updatedTask.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedTask.getUpdatedAt()).isNotNull();
        }

        @Test
        void updateTask_NonExistingTaskId_Returns404() throws Exception {
            Long missingId = 999999L;

            mockMvc.perform(put("/api/tasks/{id}", missingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "Before 6pm",
                                      "completed": true,
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Task not found"))
                    .andExpect(jsonPath("$.detail").value("Task with id 999999 was not found."))
                    .andExpect(jsonPath("$.errors", hasSize(0)));

            assertThat(taskRepository.count()).isZero();
        }

        @Test
        void updateTask_TitleIsNull_Returns400AndDoesNotModifyDatabase() throws Exception {
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());

            mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": null,
                                      "description": "Before 6pm",
                                      "completed": true,
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotNull")));

            Task unchangedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

            assertThat(taskRepository.count()).isEqualTo(1);
            assertThat(unchangedTask.getTitle()).isEqualTo("Read docs");
            assertThat(unchangedTask.getDescription()).isEqualTo("Spring MVC testing");
            assertThat(unchangedTask.getCompleted()).isFalse();
            assertThat(unchangedTask.getPriority()).isEqualTo(TaskPriority.LOW);
            assertThat(unchangedTask.getDueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(unchangedTask.getUpdatedAt()).isNull();
        }

        @Test
        void updateTask_CompletedIsNull_Returns400AndDoesNotModifyDatabase() throws Exception {
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());

            mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "Before 6pm",
                                      "completed": null,
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("completed")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("NotNull")));

            Task unchangedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

            assertThat(taskRepository.count()).isEqualTo(1);
            assertThat(unchangedTask.getTitle()).isEqualTo("Read docs");
            assertThat(unchangedTask.getDescription()).isEqualTo("Spring MVC testing");
            assertThat(unchangedTask.getCompleted()).isFalse();
            assertThat(unchangedTask.getPriority()).isEqualTo(TaskPriority.LOW);
            assertThat(unchangedTask.getDueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(unchangedTask.getUpdatedAt()).isNull();
        }

        @Test
        void updateTask_DescriptionIsTooLong_Returns400AndDoesNotModifyDatabase() throws Exception {
            String description = "a".repeat(1001);
            Task savedTask = taskRepository.save(Task.builder()
                    .title("Read docs")
                    .description("Spring MVC testing")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 7, 10))
                    .build());

            mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Buy milk",
                                      "description": "%s",
                                      "completed": true,
                                      "priority": "MEDIUM",
                                      "dueDate": "2026-06-30"
                                    }
                                    """.formatted(description)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("description")))
                    .andExpect(jsonPath("$.errors[*].code", hasItem("Size")));

            Task unchangedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

            assertThat(taskRepository.count()).isEqualTo(1);
            assertThat(unchangedTask.getTitle()).isEqualTo("Read docs");
            assertThat(unchangedTask.getDescription()).isEqualTo("Spring MVC testing");
            assertThat(unchangedTask.getCompleted()).isFalse();
            assertThat(unchangedTask.getPriority()).isEqualTo(TaskPriority.LOW);
            assertThat(unchangedTask.getDueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(unchangedTask.getUpdatedAt()).isNull();
        }
    }
}
