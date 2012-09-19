package executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private final Executor executor = Executors.newFixedThreadPool(4);

    public static final TaskExecutor getInstance() {
        return INSTANCE;
    }

    private TaskExecutor() {
        // Singleton
    }

    /** Execute asynchronously the task in context with callback */
    public void execute(TaskExecutionContext context, TaskCallback callback) {
        TaskRunner task = new TaskRunner(context, callback);
        executor.execute(task);
    }

}
