package com.renzo.todo_api.task;

import com.renzo.todo_api.task.models.Task;
import com.renzo.todo_api.task.models.TaskPriority;
import com.renzo.todo_api.task.repositories.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("dev")
public class DevTaskDataInitializer implements CommandLineRunner {
    private static final List<String> TASK_TITLES = List.of(
            "Review pull request",
            "Plan next sprint",
            "Update project documentation",
            "Refactor task service",
            "Prepare release notes",
            "Investigate production alert",
            "Schedule team retrospective",
            "Write API integration tests"
    );

    private static final List<String> DESCRIPTIONS = List.of(
            "Add any relevant details before closing this task.",
            "Coordinate with the team and record the outcome.",
            "This task was generated for manual API testing.",
            "Check the acceptance criteria before marking it complete."
    );

    private final TaskRepository taskRepository;

    public DevTaskDataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        TaskPriority[] priorities = TaskPriority.values();

        List<Task> tasks = java.util.stream.IntStream.range(0, 12)
                .mapToObj(index -> Task.builder()
                        .title(TASK_TITLES.get(random.nextInt(TASK_TITLES.size())) + " #" + random.nextInt(1000, 10_000))
                        .description(random.nextBoolean() ? DESCRIPTIONS.get(random.nextInt(DESCRIPTIONS.size())) : null)
                        .completed(random.nextBoolean())
                        .priority(random.nextBoolean() ? priorities[random.nextInt(priorities.length)] : null)
                        .dueDate(random.nextBoolean() ? LocalDate.now().plusDays(random.nextLong(-14, 61)) : null)
                        .build())
                .toList();

        taskRepository.saveAll(tasks);
    }
}
