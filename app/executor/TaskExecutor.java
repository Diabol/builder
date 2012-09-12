package executor;

import java.io.IOException;

import models.execution.Task;
import models.result.TaskResult;

public class TaskExecutor {

    private final Task task;

    public TaskExecutor(Task task) {
        this.task = task;
    }

    public TaskResult execute() {
        TaskResult result = new TaskResult(task);
        Process process = null;
        try {
            process = new ProcessBuilder(task.getCommand()).start();
            // TODO For now synchronous. We can use Executor framework later...
            process.waitFor();
            result.setResult(process);
        } catch (IOException e) {
            // TODO Logging
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Logging
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

}
