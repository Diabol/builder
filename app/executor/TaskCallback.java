package executor;

public interface TaskCallback {

    void handleTaskResult(TaskResult result);

    void handleTaskStarted(TaskExecutionContext context);

}
