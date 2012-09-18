package executor;

import java.io.IOException;

import models.config.TaskConfig;
import play.Logger;

public class Task implements Runnable {

    private final TaskExecutionContext context;
    private final TaskCallback callback;
    private TaskResult result;

    Task(TaskExecutionContext context, TaskCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void run() {
        context.startedNow();
        callback.handleTaskStarted(context);
        execute();
        context.finishedNow();
        callback.handleTaskResult(result);
    }

    private void execute() {
        Process process = null;
        try {
            // TODO: Maybe it's better to set ProcessBuilder.redirectErrorStream(true) and have only one output.
            process = new ProcessBuilder(getCommand()).start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Logger.warn("Thread for task: '" + getName() + "' interrupted while waiting for process to finish.", e);
            }
        } catch (IOException e) {
            Logger.error("Failed while running task: '" + getName() + "'.", e);
        } finally {
            if (process != null) {
                result = new TaskResult(process, context);
                process.destroy();
            } else {
                result = TaskResult.getEmptyFailedResult(context);
            }
        }
    }

    public String getName() {
        return getConfig().getTaskName();
    }

    public boolean isAutomatic() {
        return getConfig().isAutomatic();
    }

    public String getCommand() {
        return getConfig().getCommand();
    }

    private TaskConfig getConfig() {
        return context.getTask();
    }

}
