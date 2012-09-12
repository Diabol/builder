package executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import models.config.TaskConfig;


public class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private final Executor executor = Executors.newFixedThreadPool(3);

    public static final TaskExecutor getInstance() {
        return INSTANCE;
    }

    private TaskExecutor() {
        // Singleton
    }

    public void execute(TaskConfig taskConfig, ExecutionContext context, TaskCallback callback) {
        Task task = new Task(taskConfig, callback);
        executor.execute(task);
    }

}
