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
    class GetAllTasksTests {
        @Test
        void getAllTasksReturnsEmptyListWhenNoTasksExist() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void getAllTasksReturnsSavedTasks() throws Exception {
            Task task = taskRepository.save(Task.builder()
                    .title("First task")
                    .description("First description")
                    .completed(false)
                    .priority(TaskPriority.LOW)
                    .build());

            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(task.getId().intValue()))
                    .andExpect(jsonPath("$[0].title").value("First task"))
                    .andExpect(jsonPath("$[0].description").value("First description"))
                    .andExpect(jsonPath("$[0].completed").value(false))
                    .andExpect(jsonPath("$[0].priority").value("LOW"));
        }
    }

    @Nested
    class CreateTaskTests {
        @Test
        void createTaskReturnsCreatedTask() throws Exception {
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
        void createTaskReturnsCreatedTaskWhenTitleIsAtMaxLength() throws Exception {
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
        void createTaskReturnsCreatedTaskWhenDescriptionIsAtMaxLength() throws Exception {
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
        void createTaskReturnsCreatedTaskWhenOptionalFieldsAreMissing() throws Exception {
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
        void createTaskReturns400WhenTitleIsNull() throws Exception {
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
        void createTaskReturns400WhenTitleIsBlank() throws Exception {
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
        void createTaskReturns400WhenTitleIsTooLong() throws Exception {
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
        void createTaskReturns400WhenDescriptionIsTooLong() throws Exception {
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
        void createTaskReturns400WhenPriorityIsInvalid() throws Exception {
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
        void createTaskReturns400WhenBodyIsMalformed() throws Exception {
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
