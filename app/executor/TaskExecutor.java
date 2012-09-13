package executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    private final Executor executor = Executors.newFixedThreadPool(3);

    public static final TaskExecutor getInstance() {
        return INSTANCE;
    }

    private TaskExecutor() {
        // Singleton
    }

    public void execute(ExecutionContext context) {
        Task task = new Task(context);
        executor.execute(task);
    }

}
