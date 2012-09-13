package executor;

public interface TaskCallback {

    void receiveTaskResult(TaskResult result);

    void receiveTaskStarted(TaskExecutionContext context);

}
