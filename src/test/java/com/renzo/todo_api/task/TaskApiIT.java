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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
